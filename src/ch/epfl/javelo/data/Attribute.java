package ch.epfl.javelo.data;

import java.util.List;

public enum Attribute {
    // See https://wiki.openstreetmap.org/wiki/Map_features
    // and https://taginfo.openstreetmap.ch/

    // Highways (https://wiki.openstreetmap.org/wiki/Highways)
    HIGHWAY_SERVICE("highway", "service"),              //0
    HIGHWAY_TRACK("highway", "track"),                  //1
    HIGHWAY_RESIDENTIAL("highway", "residential"),      //2
    HIGHWAY_FOOTWAY("highway", "footway"),              //3
    HIGHWAY_PATH("highway", "path"),                    //4
    HIGHWAY_UNCLASSIFIED("highway", "unclassified"),    //5
    HIGHWAY_TERTIARY("highway", "tertiary"),            //6
    HIGHWAY_SECONDARY("highway", "secondary"),          //7
    HIGHWAY_STEPS("highway", "steps"),                  //8
    HIGHWAY_PRIMARY("highway", "primary"),              //9
    HIGHWAY_CYCLEWAY("highway", "cycleway"),            //10
    HIGHWAY_MOTORWAY("highway", "motorway"),             //11
    HIGHWAY_PEDESTRIAN("highway", "pedestrian"),        //12
    HIGHWAY_TRUNK("highway", "trunk"),                  //13
    HIGHWAY_LIVING_STREET("highway", "living_street"),  //14
    HIGHWAY_ROAD("highway", "road"),                    //15

    // Roads with motorway-like restrictions (https://wiki.openstreetmap.org/wiki/Key%3Amotorroad)
    MOTORROAD_YES("motorroad", "yes"),                  //16

    // Track type (https://wiki.openstreetmap.org/wiki/Key%3Atracktype)
    TRACKTYPE_GRADE1("tracktype", "grade1"),            //17
    TRACKTYPE_GRADE2("tracktype", "grade2"),            //18
    TRACKTYPE_GRADE3("tracktype", "grade3"),            //19
    TRACKTYPE_GRADE4("tracktype", "grade4"),            //20
    TRACKTYPE_GRADE5("tracktype", "grade5"),            //21

    // Surface (https://wiki.openstreetmap.org/wiki/Key%3Asurface)
    SURFACE_ASPHALT("surface", "asphalt"),              //22
    SURFACE_UNPAVED("surface", "unpaved"),              //23
    SURFACE_GRAVEL("surface", "gravel"),                //24
    SURFACE_PAVED("surface", "paved"),                  //25
    SURFACE_GROUND("surface", "ground"),                //26
    SURFACE_CONCRETE("surface", "concrete"),            //27
    SURFACE_COMPACTED("surface", "compacted"),          //28
    SURFACE_PAVING_STONES("surface", "paving_stones"),  //29
    SURFACE_GRASS("surface", "grass"),                  //30
    SURFACE_DIRT("surface", "dirt"),                    //31
    SURFACE_FINE_GRAVEL("surface", "fine_gravel"),      //32
    SURFACE_PEBBLESTONE("surface", "pebblestone"),      //33
    SURFACE_SETT("surface", "sett"),                    //34
    SURFACE_WOOD("surface", "wood"),                    //35
    SURFACE_SAND("surface", "sand"),                    //36
    SURFACE_COBBLESTONE("surface", "cobblestone"),      //37

    // One-way roads (https://wiki.openstreetmap.org/wiki/Key%3Aoneway)
    ONEWAY_YES("oneway", "yes"),                        //38
    ONEWAY_M1("oneway", "-1"),                          //39
    ONEWAY_BICYCLE_YES("oneway:bicycle", "yes"),        //40
    ONEWAY_BICYCLE_NO("oneway:bicycle", "no"),          //41

    // Vehicle access (https://wiki.openstreetmap.org/wiki/Key%3Avehicle)
    VEHICLE_NO("vehicle", "no"),                        //42
    VEHICLE_PRIVATE("vehicle", "private"),              //43

    // General access (https://wiki.openstreetmap.org/wiki/Key%3Aaccess)
    ACCESS_YES("access", "yes"),                        //44
    ACCESS_NO("access", "no"),                          //45
    ACCESS_PRIVATE("access", "private"),                //46
    ACCESS_PERMISSIVE("access", "permissive"),          //47

    // Bicycle lanes (https://wiki.openstreetmap.org/wiki/Key%3Acycleway)
    CYCLEWAY_OPPOSITE("cycleway", "opposite"),              //48
    CYCLEWAY_OPPOSITE_LANE("cycleway", "opposite_lane"),    //49
    CYCLEWAY_OPPOSITE_TRACK("cycleway", "opposite_track"),  //50

    // Bicycle access (https://wiki.openstreetmap.org/wiki/Key%3Abicycle)
    BICYCLE_YES("bicycle", "yes"),                      //51
    BICYCLE_NO("bicycle", "no"),                        //52
    BICYCLE_DESIGNATED("bicycle", "designated"),        //53
    BICYCLE_DISMOUNT("bicycle", "dismount"),            //54
    BICYCLE_USE_SIDEPATH("bicycle", "use_sidepath"),    //55
    BICYCLE_PERMISSIVE("bicycle", "permissive"),        //56
    BICYCLE_PRIVATE("bicycle", "private"),              //57

    // Bicycle route (see https://wiki.openstreetmap.org/wiki/Cycle_routes)
    ICN_YES("icn", "yes"),                              //58
    NCN_YES("ncn", "yes"),                              //59
    RCN_YES("rcn", "yes"),                              //60
    LCN_YES("lcn", "yes");                              //61

    public static final List<Attribute> ALL = List.of(values());
    public static final int COUNT = ALL.size();

    private final String key;
    private final String value;
    private final String keyValue;

    Attribute(String key, String value) {
        this.key = key;
        this.value = value;
        this.keyValue = key + "=" + value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    public String keyValue() {
        return keyValue;
    }

    @Override
    public String toString() {
        return keyValue;
    }
}