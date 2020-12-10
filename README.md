# pdf2audiobook
This is a project developed during CMPT275, a software engineering course at SFU.<br>
The application reads texts on a PDF file and generates an aduiobook.<br>
A 1500-page long textbook(1.3 million characters) only takes 5 min to convert.

## Installation
Download the source code, the repo can be imported as a IntelliJ project and you can run the application through the IDE or make a JAR or take the code in `src/main` directory and build your own executable.

### 1. Set up Google Cloud
1. Make a new project and enable text-to-speech API and Cloud functions.
2. Make two cloud storage buckets one for upload one for download.
3. For the upload bucket, make a cloud function of Event type "Finilize/Create", then paste the code in "cloudFunction" folder from source code into the Cloud function code.<br>
Make sure to choose Python 3.7 for the runtime and don't forget to copy requirements.
4. Create a service account that can read and write to your Google Storage buckets and generate a key in .json format and save that as `key.json` then place it in resources folder in the source code.<br>
[Details on how to generate a srvice account](https://cloud.google.com/iam/docs/creating-managing-service-accounts#iam-service-accounts-create-console)
### 2. Changes required in the source code
In `MainWindow.java` line 113 replace the upload and download bucket names with the ones you have created earlier.<br>
For example, if you names upload bucket "my-upload-bucket" and the download bucket "my-downoad-bucket", the line should look like `gcsAPI = new GoogleCloudAPI(keyPath, "my-upload-bucket", "my-download-bucket");`

### 3. Required directories
There are two directories required for the application to function properly.<br>
If you downloaded the repo, theses folders already exists as `resources` and `audioFiles`.
As a rule of thumb these directories should sit at the project root level.
If you decided to build a JAR, make sure that JAR is at the same directory level as these two directories.<br>
#### Sample, directory tree.
```
__ pdfToAudioBook/
 |__ myJAR.jar
 |__ audioFiles/
 |__ resources/
```

## Usage
The application requires the following to function properly.
* Stable internet connection and access to google cloud (Sorry for folks needing VPN).
* Empty folder called "audioFiles" in the same directory level as the JAR.
* Folder called "recourses" which contains key.json in the same directory level as the JAR.
* key.json this is your google service account key explained in Installation.
* Patience :)

The steps for using the application is simple. <br/>
Check out this [video](https://www.youtube.com/watch?v=Nhq4IrdsqME&feature=youtu.be) made by Hamza Kamal.

If anything isn't working, feel free to contact me via my email. aki@akizhou.com
```
macOS users may get authentication errors when launching the JAR by double clicking, try executing it via command line using java -jar
```

## Contribution
Special thanks to Hamza Kamal(H-Kamal) and Kirill Melniko(kmelnikov98) for contributing to the design, analysis and development of GUI, file queue, and text parsing.
