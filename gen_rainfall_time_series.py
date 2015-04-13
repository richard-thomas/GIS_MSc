#!/usr/bin/python
#-------------------------------------------------------------------------------
# Generate Catchment-specific Rainfall Time Series Data
# Inputs:
#     MetOffice NIMROD UK rainfall data (1km grid)
#     Catchment Areas (in ArcGIS Raster format)
#
# Richard Thomas May 2014
#-------------------------------------------------------------------------------

import os      # Standard package for operating system dependent functionality
import arcpy   # ArcGIS's Python site package
import nimrod  # Convert NIMROD format rainfall files to .asc files

# ---- Define operating parameters ----

# Name and location of catchment raster (must be in a personal geodatabase)
#catchmentRaster = "Wye_Catchment_Raster"
catchmentRaster = "Severn_Catchment_Raster"
sourceMdbPath = "work.mdb"

# Dates in order, plus hour/minute to start on first date
dateList = ["20080225", "20080226", "20080227", "20080228", "20080229",
            "20080301", "20080302", "20080303", "20080304", "20080305",
            "20080306"]
hour     = 20
minute   = 0

# Step between each reading (in minutes)
minStep  = 30

# Total number of steps
totalCount = 488

# Location / file naming of NIMROD rainfall binary files
nimrodPath = "NIMROD_data/"
nimrodSuffix = "_nimrod_ng_radar_rainrate_composite_1km_merged_UK_zip"


# ---- Perform initial calculations & checks ----

# Initialise variables
dateIx = 0
errors = 0
timeList = []

print "Creating and checking list of NIMROD files.."
while totalCount > 0:
    date = dateList[dateIx]
    timeList.append("%s%02d%02d" % (date, hour, minute))
    totalCount -= 1
    minute += minStep
    while minute >= 60:
        minute -= 60
        hour += 1
        while hour >= 24:
            hour -= 24
            dateIx += 1
            if dateIx > len(dateList):
                raise "Error: ran off end of date list"

# Run through quickly to check files all exist and are readable
for snapshot in timeList:
    nimrodFile = nimrodPath + snapshot + nimrodSuffix
    try:
        nimrodFileId = open(nimrodFile, "rb")
        nimrodFileId.close()
    except:
        print "File not found: " + nimrodFile
        errors += 1
        
if errors > 0:
    raise "Bailing out due to errors"


# ---- Start ArcMap interaction ----

class LicenseError(Exception):
    pass

# Check out Spatial Analyst Licence (if available)
print "Checking out Spatial Analyst licence.."
try:
    if arcpy.CheckExtension("Spatial") == "Available":
        arcpy.CheckOutExtension("Spatial")
    else:
        raise LicenseError

    # Throw an exception if an ArcGIS tool produces a warning
    arcpy.SetSeverityLevel(1)

    # Sets current folder as workspace
    arcpy.env.workspace = os.getcwd()

    # Allow over-writing of files
    arcpy.env.overwriteOutput = True

    # Get catchment raster from file
    catchRas = arcpy.sa.Raster(sourceMdbPath + "/" + catchmentRaster)

    # Get bounds of catchment
    extent = catchRas.extent
    
    # Sets cell size of output raster layers to be the same as catchment
    cellSize = catchRas.meanCellWidth
    
# Catch licence being unavailable
except LicenseError:
    print "Spatial Analyst license is unavailable"  

# Define column headings in output CSV file
csvOutText = "Year, Month, Date, Hour, Minute, Rainfall (mm/hr)\n"


# ---- Loop through NIMROD files to extract local rainfall data ----

for snapshot in timeList:
    nimrodFile = nimrodPath + snapshot + nimrodSuffix
    rainRasFile = catchmentRaster + "_" + snapshot + ".asc"

    try:
        # Generate .asc raster file from NIMROD data, but only make it a
        # little bit bigger than catchment as we are about to upsample it
        nimrod.nimrod2asc(nimrodFile, rainRasFile, extent.XMin, extent.XMax,
                          extent.YMin, extent.YMax)
        
        # Get catchment-specific rainfall raster from ASC file
        rainRas = arcpy.sa.Raster(rainRasFile)

        # perform (Cubic/Nearest) interpolation of 1km grid to 15m grid
        resampRainPath = sourceMdbPath + "/rainResampled"
        arcpy.Resample_management(rainRas, resampRainPath, cellSize, "CUBIC")
        #arcpy.Resample_management(rainRas, resampRainPath, cellSize, "NEAREST")
        resampRainRas = arcpy.sa.Raster(resampRainPath)

        # Select only rain that falls within catchment
        catchResampRainPath = sourceMdbPath + "/catchRainResampled"
        arcpy.gp.Con_sa(catchRas, resampRainRas, catchResampRainPath, "#", "#")
        catchResampRainRas = arcpy.sa.Raster(catchResampRainPath)

        # Get mean rainfall that falls within catchment for this timeslot
        # Also correct for fact that NIMROD data is mm/hr * 32
        rpResult = arcpy.GetRasterProperties_management(catchResampRainRas,
                                                        "MEAN","#")
        meanRainfallRate = float(rpResult.getOutput(0)) / 32.0

        # Our job for this raster is done: write mean rainfall to CSV file
        csvOutText += snapshot[0:4] + ", " + snapshot[4:6] + ", " \
                      + snapshot[6:8] + ", " + snapshot[8:10] + ", " \
                      + snapshot[10:12] + ", %f\n" % meanRainfallRate

        # Remove ASC file
        del rainRas
        os.remove(rainRasFile)
        
    # Catch warnings from geoprocessing tools (if severity level = 1)
    except arcpy.ExecuteWarning:
        print arcpy.GetMessages()

    # Catch errors from geoprocessing tools
    except arcpy.ExecuteError:
        print arcpy.GetMessages()

# Return Spatial Analyst Licence (this would happen anyway when python quits)
arcpy.CheckInExtension("Spatial")

# Write results to CSV file
outf = open(catchmentRaster + ".csv", 'w')
outf.write(csvOutText)
outf.close()

# Delete intermediate calculation rasters
# (Note: temporary raster files would be deleted when Python window or
#  ArcGIS quits anyway)
arcpy.Delete_management(resampRainRas)
arcpy.Delete_management(catchResampRainRas)

print "Done."
