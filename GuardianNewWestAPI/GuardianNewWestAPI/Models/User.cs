using System;

namespace UserList.Models
{
    public class User
    {
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