using System;

namespace GuardianNewWestAPI.Models
{
    public class Location
    {
        public const string TABLE = "Location";
        public const string COL_ID = "LocationID";
        public const string COL_USERID = "UserID";
        public const string COL_LAT = "Lat";
        public const string COL_LNG = "Lng";
        public const string COL_ALERTTIME = "AlertTime";

        public int ID { get; set; }
        public int UserID { get; set; }
        public double Lat { get; set; }
        public double Lng { get; set; }
        public DateTime AlertTime { get; set; }
    }
}