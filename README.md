# TeamCity Astronomical Trigger Plugin

This build trigger plugin for TeamCity enables builds to be added to the queue 
on a daily basis whenever the selected astronomical event occurs for the 
specified location (defined using latitude and longitude coordinates).

The astronomical events supported by the plugin include:

- Sunrise
- Sunset
- Solar Noon
- Beginning and end of Civil Twilight
- Beginning and end of Nautical Twilight
- Beginning and end of Astronomical Twilight

## Dependencies

The plugin leverages a free REST API offered by [Sunrise-Sunset](http://sunrise-sunset.org/api) 
to calculate the times of the astronomical events. 

## Plugin installation and usage

1. Install and enable the plugin by going to **Administration** >> **Plugins** and uploading the 
plugin zip file
1. Go to the Triggers screen within a Build Configuration and add a new Trigger
1. Select **Astronomical Trigger** from the list and populate the location and 
astronomical event settings
1. You can check the upcoming trigger times by clicking on the "Calculate next trigger time"
button
1. Once saved, builds will be added to the queue automatically each day based on the times returned
by the Sunrise-Sunset API.