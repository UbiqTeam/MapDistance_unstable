# MapDistance - Android App
If app crashes on startup, then be sure that app permissions for storage and location are set to allowed.

### Two modes:

* GPS- uses current GPS location to drop a marker. Drop a marker with the drop marker button.
* Manual - user touches screen at desired location to drop a marker. To use manual mode, press the GPS Mode toggle switch. The red swtich indicates that GPS mode is disabled and manual mode is enabled. Press Lock Marker for each marker.

( **NOTE**:  The user can place a mix of markers from either mode to form a quadrilateral)

**To clear the screen** press the button "Clear Map"

### Outputs

* Area in meters squared
* List of coordinates
* Lengths between markers

### Limitations

Currently only works with four markers forming a quadrilateral. If a fifth marker is placed, then the previously outlined quadrilateral will be deleted. 

### Permissions

Allow storage and location in app permissions.
