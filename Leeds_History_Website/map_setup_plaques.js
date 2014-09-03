var plaqueMap; // The map object
var myCentreLat = 53.8;
var myCentreLng = -1.55;
var initialZoom = 12;
var unknownLocationLat = 53.76;    // Dummy latitude for plaques of unknown location
var unknownLocationLng = -1.62;    // Dummy latitude for plaques of unknown location
var showUnknown = true;             // Whether to show unknown locations
var flickrBackLink = "https://www.flickr.com/photos/noii/sets/72157624096858356/"
var currentInfoWindow = null;
var markerMask = 3;     // Which markers to show (1=building, 2=person, 3=both)
var markerList = [];    // Array of all markers

// Create a click event handler function specific to each marker
function infoCallback(plaqueID, infowindow, marker) {
   return function() {
      
      // Close any already open InfoWindows
      if (currentInfoWindow != null) {
         currentInfoWindow.close();
      }
      
      // Open Pop-up InfoWindow associated with this marker
      infowindow.open(plaqueMap, marker);
      currentInfoWindow = infowindow;
      
      // Generate new HTML for Information Panel
      var infoPanelTxt = "<h2>" + marker.title + "</h2>" +
         '<a href="' + flickrBackLink + '"><img src="' +
         os_markers[plaqueID].imageURL +  '" alt="(No Image)" width="90%"></a>' +
         "<p><b>Unveiled on:</b> " + os_markers[plaqueID].date + "</p>" +
         "<p><b>Unveiled by:</b> " + os_markers[plaqueID].unveiler + "</p>" +
         "<p><b>Sponsor:</b> " + os_markers[plaqueID].sponser + "</p>" +
         "<p><b>Inscription:</b> " + os_markers[plaqueID].caption + "</p>";
         
      // Update Information Panel
      var element = document.getElementById("infobox");
      element.innerHTML = infoPanelTxt;
   };
}

function addMarker(plaqueID, unknownLoc, plaquePos) {
   var plaqueTitle = os_markers[plaqueID].title
   var plaqueMarker;

   // For unknown locations use a different icon
   if (unknownLoc == true) {
      plaqueTitle = plaqueTitle + " (Unknown Location)";
      plaqueMarker = new google.maps.Marker({
         position: plaquePos,
         map: plaqueMap,
         icon: 'exclamation.png',
         title: plaqueTitle
      });
   }
   else {
      plaqueMarker = new google.maps.Marker({
         position: plaquePos,
         map: plaqueMap,
         icon: 'http://chart.apis.google.com/chart?chst=d_map_pin_letter_withshadow&chld= |4444DD|000000',
         title: plaqueTitle
      });
   }
   
   markerList.push(plaqueMarker);
   
   // Generate HTML for Pop-up InfoWindow from marker
   var popupInfo = "<div class=infowindow><h3>" + plaqueTitle + "</h3>" +
      "Location: " + os_markers[plaqueID].location + "</div>";

   // Create a pop-up InfoWindow for this marker
   var infoWindow = new google.maps.InfoWindow({content: popupInfo});
   
   // Set an event listener for clicking on the marker to generate
   // both a pop-up InfoWindow and to update information panel
   google.maps.event.addListener(plaqueMarker, 'click',
                                 infoCallback(plaqueID, infoWindow, plaqueMarker));
}

function initialize() {

   var llPt;      // Latitude and Longitude point
   var osPt;      // OS BNG Reference Point
   
   // Render a Google Map onto map_canvas panel
   var latlng = new google.maps.LatLng(myCentreLat,myCentreLng);
   var myOptions = {
      zoom: initialZoom,
            center: latlng,
            mapTypeId: google.maps.MapTypeId.ROADMAP
   };
   plaqueMap = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
   
   // Add a marker to the map for each plaque
   for (id in os_markers) {
      
      // Get British National Grid Reference for plaque
      var easting = os_markers[id].easting;
      var northing = os_markers[id].northing;
      var unknownLoc = false;
      
      // If unknown location, generate a line of dummy markers at bottom
      if (easting == 0 || northing == 0) {
         unknownLoc = true;
         llPt = new LatLng(unknownLocationLat, unknownLocationLng);
         
         // Shift unknown location along (in a straight line)
         unknownLocationLng = unknownLocationLng + 0.01;
      }
      
      // Missing values extracted from openPlaque database are already
      // in Lng/Lat WGS84 format so just load them up
      else if (easting < 0.0) {
         llPt = new LatLng(northing, easting);
      }
      
      // Otherwise need to convert from British National Grid
      else {
         // Get BNG ref in suitable object
         osPt = new OSRef(easting, northing);
         
         // Convert BNG ref to Latitude & Longitude
         llPt = osPt.toLatLng(osPt);
         
         // Shift Datum from OSGB1936 to WGS1984 (as used by Google Maps)
         llPt.OSGB36ToWGS84();
      }
         
      // Create marker and event handlers for if it is clicked
      addMarker(id, unknownLoc, new google.maps.LatLng(llPt.lat,llPt.lng));
   }
}

// Update marker masking
function updateMarkers(plaqueType) {
  markerMask = plaqueType;
  for (var i = 0; i < markerList.length; i++) {
   
      // If unknown location, only show if flagged
      if (os_markers[i].easting == 0 || os_markers[i].northing == 0) {
         if (showUnknown) {
            markerList[i].setMap(plaqueMap);
         }
         else {
            markerList[i].setMap(null);
         }
      }
      else if ((markerMask & os_markers[i].type) > 0) {
         markerList[i].setMap(plaqueMap);
      }
      else {
          markerList[i].setMap(null);
      }
  }
}