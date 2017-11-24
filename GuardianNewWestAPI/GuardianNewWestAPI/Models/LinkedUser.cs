using System;

namespace Models.LinkedUser
{
    public class LinkedUser : IComparable
    {
        public const string PARAM_TARGET = "TargetID";
        public const string PARAM_ALERT = "Alert";
        public const string PARAM_MUTE = "Mute";
        public const string TABLE = "LinkedUser";
        public const string COL_ID = "LinkedUserID";
        public const string COL_USERID1 = "UserID1";
        public const string COL_USERID2 = "UserID2";
        public const string COL_ALERT1 = "Alert1";
        public const string COL_ALERT2 = "Alert2";
        public const string COL_MUTE1 = "Mute1";
        public const string COL_MUTE2 = "Mute2";
        public const string COL_DELETED = "Del";
        public const string COL_ADDED1 = "Added1";
        public const string COL_ADDED2 = "Added2";

        public int ID { get; set; }
        public int UserIDMe { get; set; }
        public int UserIDTarget { get; set; }
        public string NameTarget { get; set; }
        public bool AlertMe { get; set; }
        public bool AlertTarget { get; set; }
        public bool MuteMe { get; set; }
        public bool MuteTarget { get; set; }
        public bool Deleted { get; set; }
        public bool AddedMe { get; set; }
        public bool AddedTarget { get; set; }

        public int CompareTo(object obj)
        {
            LinkedUser p = obj as LinkedUser;
            if (this.UserIDTarget == p.UserIDTarget)
                return 0;
            else if (this.UserIDTarget < p.UserIDTarget)
                return -1;
            else
                return 1;
        }
    }
}