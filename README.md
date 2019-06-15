# TravelStory
is an application for geotagging and storing photos in the cloud.

# Used technologies
App uses Firebase Realtime Database, Firebase Storage, Firebase Authentication, Google Maps API.

Libraries:
 - Glide;
 - ButterKnife;
 - StfalconImageViewer.

# How to use
To use this app you need to:
 - create firebase project and add app there;
 - enable email/password & google authentication;
 - download and paste google_services.json into project folder;
 - paste your google maps api key into strings.xml.
 
# Some screenshots

## Auth
User can sign in & sign up using email/password & Google Account. User can reset password if he forgot it

![screenshot of sample](https://github.com/1stKnight/TravelStory/blob/master/auth.jpg)

## MainActivity
Used to create albums, add photos & show google map. 
Albums contain title & description. 
Photos contain category & descrition. User can write new geotag & description into photos EXIF-data.

![screenshot of sample](https://github.com/1stKnight/TravelStory/blob/master/main%20activity.jpg)


## Settings
User can enable darkmode, change password & email.

![screenshot of sample](https://github.com/1stKnight/TravelStory/blob/master/settings.png)

# Author
- **Nikita Bashan** - [1stKnight](https://github.com/1stKnight)
