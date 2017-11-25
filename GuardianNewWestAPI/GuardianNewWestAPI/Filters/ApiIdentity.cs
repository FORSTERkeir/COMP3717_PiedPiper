using System;
using System.Security.Principal;
using GuardianNewWestAPI.Models;

namespace GuardianNewWestAPI.Filters
{
    public class ApiIdentity : IIdentity
    {
        public User User
        {
            get;
            private set;
        }
        public ApiIdentity(User user)
        {
            if (user == null) throw new ArgumentNullException("user");
            this.User = user;
        }

        public string Name
        {
            get
            {
                return this.User.UserName;
            }
        }

        public string AuthenticationType
        {
            get
            {
                return "Basic";
            }
        }

        public bool IsAuthenticated
        {
            get
            {
                return true;
            }
        }
    }
}