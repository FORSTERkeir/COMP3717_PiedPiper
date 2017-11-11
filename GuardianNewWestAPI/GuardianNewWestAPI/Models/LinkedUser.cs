namespace Models.LinkedUser
{
    public class LinkedUser
    {
        public const string TABLE = "LinkedUser";
        public const string COL_ID = "LinkedUserID";
        public const string COL_USERID1 = "UserID1";
        public const string COL_USERID2 = "UserID2";
        public const string COL_ALERT1 = "Alert1";
        public const string COL_ALERT2 = "Alert2";
        public const string COL_MUTE1 = "Mute1";
        public const string COL_MUTE2 = "Mute2";
        public const string COL_DELETED = "Del";
        public const string COL_CONFIRMED = "Confirmed";

        public int ID { get; set; }
        public int UserID1 { get; set; }
        public int UserID2 { get; set; }
        public bool Alert1 { get; set; }
        public bool Alert2 { get; set; }
        public bool Mute1 { get; set; }
        public bool Mute2 { get; set; }
        public bool Deleted { get; set; }
        public bool Confirmed { get; set; }
    }
}