# Data Analysis
-----------------
A little of Data Analysis is done in R in this folder.

## Usage
To start the script, issue the following command, making sure that `r-base` package is installed into your linux system. Of course, you need to provide your *connection string*.
```bash
    Rscript Analyze.r
```

#### Use it with your MongoDB installation/cloud
If you want to use this script with your MongoCluster, make sure to change the following lines into [Analyze.r](https://github.com/CarloP95/GPSChat/blob/master/DataAnalysis/Analyze.r#L77)

```R
username = "MongoUsername"
pwd = "MongoPWD"
urlPath = sprintf("mongodb+srv://%s:%s@MongoConnectionString", username, pwd)
```

And these lines found here [Analyze.r](https://github.com/CarloP95/GPSChat/blob/master/DataAnalysis/Analyze.r#L119) with your [LocationIQ](https://locationiq.com/) token (Free).

```R
#Get cities from latlon objects
locations <- shouts$location
apiToken <- "ApiTokenFromLocationIQ"
locationIQEndpoint <- sprintf("https://eu1.locationiq.com/v1/reverse.php?key=%s", apiToken)
lIQQuery <- "&lat=%f&lon=%f&format=json"
```

## What kind of Data Analysis
With this script the following data will be displayed into pdf.
1. Most active hour for the application : [Messages per Hour](#messagesPerHour).
2. Most active cities for the application : [Most Active Cities](#mostActiveCities).
3. Total number of shouts and replies per user : [R Plots](#rPlots).

#### Messages Per Hour
![MessagesPerHour](res/MessagesPerHour.pdf)

#### Most Active Cities
![MessagesPerHour](res/MostActiveCities.pdf)

#### R Plots
![Plots](Rplots.pdf)