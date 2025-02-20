# monday-drive-backup
Code for backing up Monday.com boards to Google Drive

The Monday.com Board Id and the Google Drive folder Id must be configured in config.properties

Google Credentials can be downloaded as a json file from the GCP Portal:
https://console.cloud.google.com/home/dashboard?project=bagsoftaste&inv=1&invt=AbqG-w&organizationId=0

Note that a service principal must be provisioned eg. upload@bagsoftaste.iam.gserviceaccount.com 

The credential file should be encoded to base 64 as follows: 
cat google-credentials.json | base64 | tr -d '\n' > encoded_credentials.txt

The base64 encoded string must then be setup as an environment variable.  
When deployed as a GitHub action this would be stored as a secret and set at run time.

Similarly, the Monday.com API key must be stored as a secret and set as an Env variable at run time.

It's assumed that a Google Drive folder is used and the folder id configured.
Files will be uploaded to the folder specified by the folder's id (also in config.properties), 
eg https://drive.google.com/drive/folders/1trIfU53wfwdwDi17T3aFfDjp6TYRyPo1
The folder must be shared with the service principal defined earlier and it must be given Editor access.
