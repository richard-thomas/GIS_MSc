var buildingMap; // The map object
var myCentreLat = 53.8;
var myCentreLng = -1.55;
var initialZoom = 11;
var currentInfoWindow = null; // Currently open pop-up information window
var markerList = [];          // Array of all map markers
var infoWindowList = [];      // List of all infoWindow objects

// Create a click event handler function specific to each marker
function infoCallback(buildingID, infowindow, marker) {
   return function() {
      
      // Close any already open InfoWindow
      closeInfoWindow();
     
      // Open Pop-up InfoWindow associated with this marker
      infowindow.open(buildingMap, marker);
      currentInfoWindow = infowindow;
   };
}

// Add new marker to map
function addMarker(buildingID, buildingPos) {
   var buildingTitle = buildings[buildingID].name
   var buildingMarker;
   var markerIcon;

   // For buildings at risk use a different icon
   if (buildings[buildingID].risk == 0)
      markerIcon = 'http://chart.apis.google.com/chart?chst=d_map_pin_letter_withshadow&chld= |44DD44|000000';
   else if (buildings[buildingID].risk == 1)
      markerIcon = 'http://chart.apis.google.com/chart?chst=d_map_pin_letter_withshadow&chld= |CCCC44|000000';
   else
      markerIcon = 'http://chart.apis.google.com/chart?chst=d_map_pin_letter_withshadow&chld= |DD4444|000000';

   buildingMarker = new google.maps.Marker({
      position: buildingPos,
      map: buildingMap,
      icon: markerIcon,
      title: buildingTitle
   });
   
   markerList.push(buildingMarker);
   
   // Generate HTML for Pop-up InfoWindow from marker
   var popupInfo = "<div class=infowindow><h3>" + buildingTitle +
      "</h3>Grade " + buildings[buildingID].grd +
      ". Built between: " + buildings[buildingID].tlow + " and " +
      buildings[buildingID].thi +
      "<p>Details: <a href='http://www.imagesofengland.org.uk/Details/Default.aspx?id=" +
      buildings[buildingID].legid + "' target='_blank'>(Images of England)</a> " +
      "<a href='http://list.english-heritage.org.uk/resultsingle.aspx?uid=" +
      buildings[buildingID].id + "' target='_blank'>(English Heritage)</a>";
   if (buildings[buildingID].vwid >= 0) {
      popupInfo += " <a href='http://www.victorianweb.org/art/architecture/leeds/index.html' " +
         "target='_blank'>(Victorian Web)</a>";
   }
   popupInfo += "</p></div>";

   // Create a pop-up InfoWindow for this marker
   var infoWindow = new google.maps.InfoWindow({content: popupInfo});
   
   // Set an event listener for clicking on the marker to generate
   // both a pop-up InfoWindow and to update information panel
   google.maps.event.addListener(buildingMarker, 'click',
                                 infoCallback(buildingID, infoWindow, buildingMarker));
   
   // Save pointers to infoWindows to allow call from search
   infoWindowList.push(infoWindow);
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
   buildingMap = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
   
   // Add a marker to the map for each building
   for (id in buildings) {
      
      // Get British National Grid Reference for building
      var easting = buildings[id].e;
      var northing = buildings[id].n;
      
      // Convert from British National Grid
      // Get BNG ref in suitable object
      osPt = new OSRef(easting, northing);
      
      // Convert BNG ref to Latitude & Longitude
      llPt = osPt.toLatLng(osPt);
      
      // Shift Datum from OSGB1936 to WGS1984 (as used by Google Maps)
      llPt.OSGB36ToWGS84();
      
      // Create marker and event handlers for if it is clicked
      addMarker(id, new google.maps.LatLng(llPt.lat,llPt.lng));
   }
}

// Update marker masking
function updateMarkers() {
   var builtFrom = document.getElementById("yearFrom").value;
   var builtTo = document.getElementById("yearTo").value;

   // Close any already open InfoWindow
   closeInfoWindow();
   
   for (var i = 0; i < markerList.length; i++) {
   
      // If within construction date range show marker
      if (builtTo >= buildings[i].tlow && builtFrom <= buildings[i].thi) {
         markerList[i].setMap(buildingMap);
      }
      else {
          markerList[i].setMap(null);
      }
  }
}

// Search for building
function buildingSearch(substring) {
   
   // Close any already open InfoWindow
   closeInfoWindow();
   
   var searchPanelTxt = "";
   for (var i = 0; i < buildings.length; i++) {
      
      // For each matching building add it to list and display its marker
      if (buildings[i].name.search(substring.toUpperCase()) >= 0) {
         
         // Use appropriately coloured marker icon
         if (buildings[i].risk == 0)
            searchPanelTxt += "<p><img src='green-dot.png' width=20 alt='Building Marker'";
         else if (buildings[i].risk == 1)
            searchPanelTxt += "<p><img src='yellow-dot.png' width=20 alt='Building Marker'";
         else
            searchPanelTxt += "<p><img src='red-dot.png' width=20 alt='Building Marker'";

         // Set up callback function to highlight just that marker
         searchPanelTxt += " onclick='highlightMarker(" + i + ")';>" +
            buildings[i].name + "</p>\n";
         markerList[i].setMap(buildingMap);
      }
      
      // Turn off other map markers
      else {
          markerList[i].setMap(null);
      }
   }
   if (searchPanelTxt.length == 0) {
      searchPanelTxt = "(No matches found)";
   }
   else {
      searchPanelTxt = "Click icon in list to select on map..\n" + searchPanelTxt;
   }
   document.getElementById("hotlinks").innerHTML = searchPanelTxt;
}

// Highlight only selected marker from search
function highlightMarker(arrayIndex) {
   
   // Close any already open InfoWindow
   closeInfoWindow();
   
   for (var i = 0; i < markerList.length; i++) {
   
      // If within construction date range show marker
      if (i == arrayIndex) {
         markerList[i].setMap(buildingMap);
         
         // Open Pop-up InfoWindow associated with this marker
         currentInfoWindow = infoWindowList[i];
         currentInfoWindow.open(buildingMap, markerList[i]);
      }
      else {
          markerList[i].setMap(null);
      }
   }
}

// Close any already open InfoWindow
function closeInfoWindow() {
   if (currentInfoWindow != null) {
      currentInfoWindow.close();
   }
}
