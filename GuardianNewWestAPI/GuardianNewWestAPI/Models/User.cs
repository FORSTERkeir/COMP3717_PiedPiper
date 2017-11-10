using System;

namespace Models.User
{
    public class User
    {
        public const string TABLE = "User";
        public const string COL_ID = "UserID";
        public const string COL_USERNAME = "UserName";
        public const string COL_EMAIL = "Email";
        public const string COL_PASSWORD = "Password";
        public const string COL_PHONE = "Phone";
        public const string COL_LOGIN = "Login";
        public const string COL_LASTLOGIN = "LastLogin";
        public const string COL_STATUS = "StatusTypeID";
        public const string COL_DELETED = "Del";

        public int ID { get; set; }
        public string UserName { get; set; }
        public string Email { get; set; }
        public string Password { get; set; }
        public long Phone { get; set; }
        public bool Login { get; set; }
        public DateTime LastLogin { get; set; }
        public int Status { get; set; }
        public bool Deleted { get; set; }
    }
}