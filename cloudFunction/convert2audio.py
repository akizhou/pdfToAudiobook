import time
import math
import tempfile
from random import randint as rand
from concurrent.futures import ThreadPoolExecutor
from itertools import chain
from google.cloud import storage
from google.cloud import texttospeech

STORAGE_CLIENT = storage.Client()
SPEECH_CLIENT = texttospeech.TextToSpeechClient()
OUT_BUCKET = STORAGE_CLIENT.bucket("converted-audio-files")

def gcs_triggered(file, context):
    # Get blob from event trigger bucket
    print("Setting up")
    rawFilename = file["name"]
    rawBucket = None
    rawBlob = None
    while rawBucket == None or rawBlob == None:
        print("In loop")
        rawBucket = STORAGE_CLIENT.get_bucket(file["bucket"])
        rawBlob = rawBucket.get_blob(rawFilename)
        time.sleep(1)
        
    # Parse options
    print("Parsing options")
    rawFilename, options = rawFilename[:-25], rawFilename[-25:]
    options = options.replace("-","").replace("(","").replace(")","")
    options = options.split(",")
    
    # Conversion
    if rawFilename.lower().endswith(".txt"):
        print("Starting conversion")
        convert_to_audio(rawBlob, rawFilename[:-4], options)
    else:
        raise ValueError("Not a .txt, file type is not allowed")
    
    return


def convert_to_audio(rawBlob, fileBasename: str, options: list):
    totalStart = time.time()
    # Make a temporary folder to store mp3 files to be merged
    print("Making a temporary folder")
    dirName = fileBasename + "/"
    
    # Open rawBlob as a file and generate speech line by line 
    # then put generated mp3 to the temporary folder
    print("Generating speech files")
    with tempfile.TemporaryFile() as textFile:
        rawBlob.download_to_file(textFile)
        textFile.seek(0)
        lines = textFile.readlines()
        
        start = time.time()
        mp3BlobList = concurrent_generate_speech(fileBasename + "/", lines, options)
        end = time.time()
        print("Total conversion took: {t} seconds".format(t = (end-start)))
    
    # Merge all mp3 files in folder
    print("Merging speech files")
    merge_MP3(mp3BlobList, fileBasename)
    
    totalEnd = time.time()
    print("Total execution time: {t} seconds".format(t = (totalEnd-totalStart))) 


def concurrent_generate_speech(directory: str, lines: list, options: list):
    RATE_LIMIT_TIME = 60  # Seconds
    numThreads = 300  # The absolute max is 300 = max number of requests per minute
    numBatch = math.ceil(len(lines)/numThreads)
    spinUpDelay = RATE_LIMIT_TIME / numThreads  # Maximum delay that still have all threads started with in RATE_LIMIT_TIME
    
    lineID = 0
    mp3BlobList = []
    for batchID in range(numBatch):
        tic = time.time()
        
        # Start a batch of threads
        with ThreadPoolExecutor(max_workers=numThreads) as executor:
            threadCount = 0
            futures = []
            # Create and start threads
            while lineID < len(lines):
                futures.append(executor.submit(generate_speech, lineID, directory, lines[lineID], options, (RATE_LIMIT_TIME-(spinUpDelay*threadCount+1))))
                time.sleep(spinUpDelay) # Sleep to avoid overlapping request timing
                
                lineID += 1
                threadCount += 1
                if threadCount >= numThreads:
                    break
            
            # Wait for threads
            for i in futures: mp3BlobList.append(i.result())
            
        toc = time.time()
        
        # Sleep to avoid going over the rate limit
        print("Batch {i} finished in: {t} seconds".format(i = batchID, t = (toc-tic)))
        if((toc-tic) < RATE_LIMIT_TIME and (batchID+1) < numBatch):
            sleepDuration = RATE_LIMIT_TIME - (toc-tic)
            print("sleeping for: {t}".format(t = sleepDuration))
            time.sleep(sleepDuration)
            
    return mp3BlobList

def generate_speech(ID: int, directory: str, textToRead: str, options: list, initialBackoff: float):
    # Set the text input to be synthesized
    synthesis_input = texttospeech.SynthesisInput(text=textToRead)
    
    # Set voice options
    lanCode = options[0].replace("_", "-")
    name = map_googles_bad_naming(lanCode, options[1])  # Dear Google, Why is your voices not named consistently?
         
    voice = texttospeech.VoiceSelectionParams(language_code=lanCode, name=name)
    
    # Set the type of audio file
    audio_config = texttospeech.AudioConfig(audio_encoding=texttospeech.AudioEncoding.MP3,
                                            speaking_rate=float(options[2]), pitch=float(options[3].replace("n", "-")))
   
    # Generate speech
    attempt = 0
    MAX_BACKOFF = 32
    while True:
        try:
            response = SPEECH_CLIENT.synthesize_speech(input=synthesis_input, voice=voice, audio_config=audio_config)
        except Exception as exc:
            print("Retrying speech generation for line {l}. Attempt: {a}".format(l = ID, a = attempt))
            if attempt == 0:
                # Retry once all threads are started to avoid chain reaction of overlappings and delays
                time.sleep(initialBackoff)
            else:
                # Exponential backoff
                time.sleep(min((2**attempt)+rand(0,1000)*0.001, MAX_BACKOFF))
            attempt += 1
            continue
        break
    
    # Save response as a mp3 file
    mp3Filename = str(ID) + ".mp3"
    mp3Blob = OUT_BUCKET.blob(directory + mp3Filename)
    mp3Blob.upload_from_string(response.audio_content, content_type="audio/mpeg")
            
    return mp3Blob


def merge_MP3(mp3BlobList: list, fileBasename: str):
    print("Writing to output file")
    mergeList = []
    for i in range(math.ceil(len(mp3BlobList)/32)):
        name = fileBasename + "/merge-" + str(i) + ".mp3"
        mergedBlob = OUT_BUCKET.blob(name)
        mergedBlob.compose(mp3BlobList[i*32:(i+1)*32])
        mergeList.append(mergedBlob)
        
    if len(mergeList) <= 32:
        outFilename = fileBasename + ".mp3"
        mergedMP3Blob = OUT_BUCKET.blob(outFilename)
        mergedMP3Blob.compose(mergeList)
        
        # Delete temporary files
        print("Cleaning up temporary files")
        clean_temp_files(mp3BlobList)
        clean_temp_files(mergeList)
    else:
        secondaryMergeList = []
        for j in range(math.ceil(len(mergeList)/32)):
            name = "{base}/merge2-{ID}.mp3".format(base = fileBasename, ID = j)
            secondaryMergedBlob = OUT_BUCKET.blob(name)
            secondaryMergedBlob.compose(mergeList[j*32:(j+1)*32])
            secondaryMergeList.append(secondaryMergedBlob)
        
        if len(secondaryMergeList) <= 32:
            outFilename = fileBasename + ".mp3"
            mergedMP3Blob = OUT_BUCKET.blob(outFilename)
            mergedMP3Blob.compose(secondaryMergeList)
            
            # Delete temporary files
            print("Cleaning up temporary files")
            clean_temp_files(mp3BlobList)
            clean_temp_files(mergeList) 
            clean_temp_files(secondaryMergeList)
        else:
            print("Wow big list, need to fix merge_MP3")
        
        
def clean_temp_files(blobs: list):
    with ThreadPoolExecutor(max_workers=len(blobs)) as executor:
        executor.map(delete_wrapper, blobs)
    

def delete_wrapper(blob):
    attempt = 0
    while True:
        try:
            blob.delete()
        except Exception as exc:
            print("Retrying deletion. Attempt: " + str(attempt))
            attempt += 1
            continue
        break
    
    
def map_googles_bad_naming(lanCode: str, gender: str):
    # Sadly I do not have the $ for using Wavenet voices but there is not parameter for
    # choosing between standard or Wavenet so I had to hard code this ridiculous function
    # Also google, why isn't female voice just named with A and B for male? It did give one
    # more purpose to this stupid function but I wish it was more consistent.
    
    FEMALE_VOICES = {
        "ar-XA": "ar-XA-Standard-A",
        "bn-IN": "bn-IN-Standard-A",
        "yue-HK": "yue-HK-Standard-A",
        "cs-CZ": "cs-CZ-Standard-A",
        "da-DK": "da-DK-Standard-A",
        "nl-NL": "nl-NL-Standard-A",
        "en-AU": "en-AU-Standard-A",
        "en-IN": "en-IN-Standard-A",
        "en-GB": "en-GB-Standard-A",
        "en-US": "en-US-Standard-C",
        "fil-PH": "fil-PH-Standard-A",
        "fi-FI": "fi-FI-Standard-A",
        "fr-CA": "fr-CA-Standard-A",
        "fr-FR": "fr-FR-Standard-A",
        "de-DE": "de-DE-Standard-A",
        "el-GR": "el-GR-Standard-A",
        "gu-IN": "gu-IN-Standard-A",
        "hi-IN": "hi-IN-Standard-A",
        "hu-HU": "hu-HU-Standard-A",
        "id-ID": "id-ID-Standard-A",
        "it-IT": "it-IT-Standard-A",
        "ja-JP": "ja-JP-Standard-A",
        "kn-IN": "kn-IN-Standard-A",
        "ko-KR": "ko-KR-Standard-A",
        "ml-IN": "ml-IN-Standard-A",
        "cmn-CN": "cmn-CN-Standard-A",
        "nb-NO": "nb-NO-Standard-A",
        "pl-PL": "pl-PL-Standard-A",
        "pt-BR": "pt-BR-Standard-A",
        "pt-PT": "pt-PT-Standard-A",
        "ru-RU": "ru-RU-Standard-A",
        "sk-SK": "sk-SK-Standard-A",
        "es-ES": "es-ES-Standard-A",
        "sv-SE": "sv-SE-Standard-A",
        "ta-IN": "ta-IN-Standard-A",
        "te-IN": "te-IN-Standard-A",
        "th-TH": "th-TH-Standard-A",
        "tr-TR": "tr-TR-Standard-A",
        "uk-UA": "uk-UA-Standard-A",
        "vi-VN": "vi-VN-Standard-A"
    }
    
    MALE_VOICES = {
        "ar-XA": "ar-XA-Standard-B",
        "bn-IN": "bn-IN-Standard-B",
        "yue-HK": "yue-HK-Standard-B",
        "da-DK": "da-DK-Standard-C",
        "nl-NL": "nl-NL-Standard-B",
        "en-AU": "en-AU-Standard-B",
        "en-IN": "en-IN-Standard-B",
        "en-GB": "en-GB-Standard-B",
        "en-US": "en-US-Standard-B",
        "fil-PH": "fil-PH-Standard-C",
        "fr-CA": "fr-CA-Standard-B",
        "fr-FR": "fr-FR-Standard-B",
        "de-DE": "de-DE-Standard-B",
        "gu-IN": "gu-IN-Standard-B",
        "hi-IN": "hi-IN-Standard-B",
        "id-ID": "id-ID-Standard-B",
        "it-IT": "it-IT-Standard-C",
        "ja-JP": "ja-JP-Standard-C",
        "kn-IN": "kn-IN-Standard-B",
        "ko-KR": "ko-KR-Standard-C",
        "ml-IN": "ml-IN-Standard-B",
        "cmn-CN": "cmn-CN-Standard-B",
        "nb-NO": "nb-NO-Standard-B",
        "pl-PL": "pl-PL-Standard-B",
        "pt-PT": "pt-PT-Standard-B",
        "ru-RU": "ru-RU-Standard-B",
        "es-ES": "es-ES-Standard-B",
        "ta-IN": "ta-IN-Standard-B",
        "te-IN": "te-IN-Standard-B",
        "tr-TR": "tr-TR-Standard-B",
        "vi-VN": "vi-VN-Standard-B"
    }
    
    if gender == "MALE" and (lanCode in MALE_VOICES.keys()):
        return MALE_VOICES[lanCode]
    else:
        return FEMALE_VOICES[lanCode]