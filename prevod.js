//top-left reference point
var p0 = {
    scrX: 23.69,        // Minimum X position on screen
    scrY: -0.5,         // Minimum Y position on screen
    lat: -22.814895,    // Latitude
    lng: -47.072892     // Longitude
}
//bottom-right reference point
var p1 = {
    scrX: 276,          // Maximum X position on screen
    scrY: 178.9,        // Maximum Y position on screen
    lat: -22.816419,    // Latitude
    lng: -47.070563     // Longitude
}
var radius = 6.371;     //Earth Radius in Km

//## Now I can calculate the global X and Y for each reference point ##\\

// This function converts lat and lng coordinates to GLOBAL X and Y positions
function latlngToGlobalXY(lat, lng){
    //Calculates x based on cos of average of the latitudes
    let x = radius*lng*Math.cos((p0.lat + p1.lat)/2);
    //Calculates y based on latitude
    let y = radius*lat;
    return {x: x, y: y}
}
// Calculate global X and Y for top-left reference point
p0.pos = latlngToGlobalXY(p0.lat, p0.lng);
// Calculate global X and Y for bottom-right reference point
p1.pos = latlngToGlobalXY(p1.lat, p1.lng);

/*
* This gives me the X and Y in relation to map for the 2 reference points.
* Now we have the global AND screen areas and then we can relate both for the projection point.
*/

// This function converts lat and lng coordinates to SCREEN X and Y positions
function latlngToScreenXY(lat, lng){
    //Calculate global X and Y for projection point
    let pos = latlngToGlobalXY(lat, lng);
    //Calculate the percentage of Global X position in relation to total global width
    pos.perX = ((pos.x-p0.pos.x)/(p1.pos.x - p0.pos.x));
    //Calculate the percentage of Global Y position in relation to total global height
    pos.perY = ((pos.y-p0.pos.y)/(p1.pos.y - p0.pos.y));

    //Returns the screen position based on reference points
    return {
        x: p0.scrX + (p1.scrX - p0.scrX)*pos.perX,
        y: p0.scrY + (p1.scrY - p0.scrY)*pos.perY
    }
}

//# The usage is like this #\\

var pos = latlngToScreenXY(-22.815319, -47.071718);
console.log("posx " + pos.x)
console.log("posy " + pos.y)
/*$point = $("#point-to-project");
$point.css("left", pos.x+"em");
$point.css("top", pos.y+"em");*/