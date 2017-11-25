namespace GuardianNewWestAPI.Models
{
    public class EmergencyBldg
    {
        public const string TABLE = "EmergencyBldg";
        public const string COL_ID = "EmergencyBldgID";
        public const string COL_CATEGORY = "CatTypeID";
        public const string COL_BLDGID = "BldgID";
        public const string COL_BLDGNAME = "BldgName";
        public const string COL_STRNUM = "StrNum";
        public const string COL_STRNAME = "StrName";
        public const string COL_MAPREF = "MapRef";
        public const string COL_LAT = "Lat";
        public const string COL_LNG = "Lng";
        public const string COL_PHONE = "Phone";
        public const string COL_LOCNAME = "LocName";

        public int ID { get; set; }
        public int Category { get; set; }
        public int BldgID { get; set; }
        public string BldgName { get; set; }
        public int StrNum { get; set; }
        public string StrName { get; set; }
        public int MapRef { get; set; }
        public double Lat { get; set; }
        public double Lng { get; set; }
        public long Phone { get; set; }
        public string LocName { get; set; }
    }
}