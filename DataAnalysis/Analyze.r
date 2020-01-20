#install.packages("devtools")
#install.packages("mongolite")
#install.packages("stringr")
#devtools::install_github("https://github.com/r-lib/httr")
library(mongolite)
library(stringr)
library(httr)


getMessagesForUser <- function (dataframe) {
	
	return (table(dataframe[1]))

}

#Wrapper for plotting barplot with users that uses the most the application
plotBarsForMaxUsageInMsg <- function(table, title, typeOfMessage, color = "firebrick4") {
	barplot(table, main = title, xlab = "Nickname", ylab = sprintf("#%s", typeOfMessage), 
		col = color, border = "deepskyblue")
}

#Will not use Day, Month Year and Timezone
simplifyTimestamp <- function (timestamp) {
	timeLessYear <- stringr::str_trunc(timestamp, 19, ellipsis = "")
	onlyTime <- stringr::str_trunc(timeLessYear, 8, side = "left", ellipsis = "")
	year <- stringr::str_trunc(timestamp, 4, side = "left", ellipsis = "")
	glued <- paste(onlyTime, year)
	return (glued)
}

#With correct format, get R interpretable timestamp
castTimestamp <- function (timestamp) {
	return (strptime(timestamp, format = "%H:%M:%S %Y"))
}

#Get a data frame with messages per hour
getNumMessagesPerHour <- function (mat) {
	returnDF <- as.data.frame(matrix(0, ncol = 24, nrow = 1))
	hours <- mat[, "hour"]
	for (singleHour in hours) {
		if (is.na (singleHour)) {
			next
		}
		returnDF[singleHour] = returnDF[singleHour] + 1
	}

	return (returnDF)
}


plotNumMessagePerHour <- function (dataFrame) {
	pdf("res/MessagesPerHour.pdf")
	hourLabels <- vector()
	for (hour in seq(from = 0, to = 23)) {
		hourLabels <- c (hourLabels, sprintf("%s:00", hour))
	}
		
	barplot ( t(as.matrix(dataFrame)), beside = TRUE, main = "Usage of GPSChat per Hours",
		col = "firebrick4", border = "grey", xlab = "Hours of Day", ylab = "Num messages", 
		names.arg = hourLabels )	
	dev.off()

}


plotMostActiveCities <- function (table) {
		pdf("res/MostActiveCities.pdf")
		barplot ( table, main = "Messages Per city",
			col = "grey", border = "firebrick4", 
			xlab = "Name of the city", ylab = "Num messages")
		dev.off()

}

print("Starting script to analyze data in MongoDB for GPSChat")

username = "MongoUsername"
pwd = "MongoPWD"
urlPath = sprintf("mongodb+srv://%s:%s@MongoConnectionString", username, pwd)

collectionName = "messages"
dbName ="gpschat"

mc <- mongo( collection=collectionName, db=dbName, url=urlPath, verbose=TRUE )

numEntries = mc$count()

shouts = mc$find( query = '{ "type" : { "$eq" : 0 } }' )
replies = mc$find( query = '{ "type" : { "$eq" : 2 } }' )

#Getting from dataframe
shout_NickNameAndMessages = shouts[ c(8,9) ]
replies_NickNameAndMessages = replies[ c(8,9) ]

#Get tables
shoutPerUser = getMessagesForUser(shout_NickNameAndMessages)
replyPerUser = getMessagesForUser(replies_NickNameAndMessages)

#Plot barplots
plotBarsForMaxUsageInMsg (shoutPerUser, "Total number of shouts per user", "Shouts")
plotBarsForMaxUsageInMsg (replyPerUser, "Total number of replies per user", "Replies", "darkred")

#Get timestamps
shout_timestamps <- shouts[2]
reply_timestamps <- replies[2]
timestamps <- rbind(shout_timestamps, reply_timestamps)

#Allocate matrix with ncol of number of attributes of object timestamp
correctedTimestamps <- matrix(ncol=11)
for (idx in seq (from = 1, to = nrow (timestamps) ) ) {
	currentTimestamp <- simplifyTimestamp (timestamps[idx, ])
	castInTime <- castTimestamp (currentTimestamp)
	correctedTimestamps <- rbind(correctedTimestamps, castInTime)
}

messagesPerHour <- getNumMessagesPerHour (correctedTimestamps)
plotNumMessagePerHour (messagesPerHour)

#Get cities from latlon objects
locations <- shouts$location
apiToken <- "ApiTokenFromLocationIQ"
locationIQEndpoint <- sprintf("https://eu1.locationiq.com/v1/reverse.php?key=%s", apiToken)
lIQQuery <- "&lat=%f&lon=%f&format=json"

endpoints <- c()
for (index in seq(to =  nrow(locations))) {
	currentLoc <- locations[index, ]
	completedQuery <- sprintf (lIQQuery, currentLoc$latitude, currentLoc$longitude)
	endpoints <- c (endpoints, paste (locationIQEndpoint, completedQuery, sep = "") )
}

cities <- c()
#OpenStreetMap will return NULL for subsequent request that contains the same query
# This will allow to get the city in every case
lastNonNullCity <- NULL
for (endpoint in endpoints) {
	response <- httr::GET (endpoint)
	rContent <- httr::content (response, as = "parsed")
	city <- rContent$address$city
	if ( is.null (city) ) {
		city <- lastNonNullCity	
	}
	lastNonNullCity <- city
	cities <- c (cities, city)
}

plotMostActiveCities (table (cities)) 

# Disconnect at the end
mc$disconnect()
