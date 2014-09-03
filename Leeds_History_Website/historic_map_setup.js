var mapOverlays = [];     // Array of map overlays
var currentOverlay = null;// Currently selected map overlay
var visible = true;       // Visibility of current overlay

// Map Starting location and zoom level
var centreLat = 53.8;
var centreLong = -1.55;
var initialZoom = 12;

// Define the map object
var historicMap;

/*
 * initialize() function - called on page load
 */
function initialize() {

  var latlng = new google.maps.LatLng(centreLat, centreLong);
  var myOptions = {
    zoom: initialZoom,
    center: latlng,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };
  historicMap = new google.maps.Map(document.getElementById("map_canvas"),myOptions);

  // OS First Series 1858 Map Overlay
  createOverlay("map_data/OS_1st_series_1841_1858.jpg",
                53.74305, -1.6039,  // SW Corner
                53.8420, -1.4191);  // NE Corner
  // Bartholomew 1866 Map Overlay
  createOverlay("map_data/Leeds_1866_by_J_Bartholemew_N_aligned3.gif",
                53.7785, -1.592,  // SW Corner
                53.8175, -1.507);  // NE Corner
  // OS New Series 1903 Map Overlay
  createOverlay("map_data/OS_New_Series_233440_1903.jpg",
                53.667, -2.0,  // SW Corner
                54.0, -1.333);  // NE Corner
  // OS New Popular Edition 1945 Map Overlay
  createOverlay("map_data/NPE_63360_sheet96_1945.jpg",
                53.75, -1.669,  // SW Corner
                53.9, -1.402);  // NE Corner
 
    
  // Create listener for mouse click on base google map
  google.maps.event.addListener(historicMap, 'click', function(event) {
      toggleVisibility(event);
    });

  // Turn on initial overlay
  selectMap(0);
}

/*
 * Create a new map overlay from specified image and coords
 */
function createOverlay(imgFile, swLat, swLong, neLat, neLong) {
  
  // Define Latitude and Longitude bounds of image
  var imageBounds = new google.maps.LatLngBounds(
      new google.maps.LatLng(swLat, swLong),  // SW corner
      new google.maps.LatLng(neLat, neLong)   // NE corner
    );
  
  // Create map overlay from image and append to array
  var overlay = new google.maps.GroundOverlay(imgFile, imageBounds);
  mapOverlays.push(overlay);
  
  // Create listener for mouse click on overlay
  google.maps.event.addListener(overlay, 'click', function(event) {
      toggleVisibility(event);
    });
}

/*
 * Select one of the overlays
 */
function selectMap(overSelect) {
  
  // Unselect any previous overlay
  if (currentOverlay != null) {
    currentOverlay.setMap(null);
  }
  
  currentOverlay = mapOverlays[overSelect];
  
  // Make new overlay visible
  currentOverlay.setMap(historicMap);
  visible = true;
}

/*
 * Switch current overlay on/off (called on mouse click events)
 */
function toggleVisibility(event) {
  
  // DEBUG: Record mouse click location to console
  console.log("Location: " + event.latLng);
  
  if (visible) {
    visible = false;
    currentOverlay.setMap(null);
  }
  else {
    visible = true;
    currentOverlay.setMap(historicMap);
  }
}