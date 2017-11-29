using System;
using System.Web.Http;
using System.Data.SqlClient;
using System.Data;
using System.Collections;
using System.Linq;
using GuardianNewWestAPI.Models;
using GuardianNewWestAPI.Utilities;
using GuardianNewWestAPI.Filters;
using System.Threading.Tasks;
using System.Web;
using System.Net.Http;
using System.Net;
using Microsoft.Azure.NotificationHubs;
using System.Net.Http.Headers;

namespace GuardianNewWestAPI.Controllers
{
    public class NotificationController : ApiController
    {
        private const int STATUS_NORMAL = 5;
        private const int STATUS_ALERT = 6;
        private const string TAG_PREFIX = "email";

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/alert")]
        public IHttpActionResult Alert([FromBody] object data)
        {
            LinkedUser lu;
            ArrayList linkedUsers = new ArrayList();
            ArrayList emailsToAlert = new ArrayList();
            string userName = string.Empty;

            try
            {
                var headers = Request.Headers;
                string id = headers.GetValues(Models.User.COL_ID).First();
                string lat = headers.GetValues(Location.COL_LAT).First();
                string lng = headers.GetValues(Location.COL_LNG).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        // Set flag for user table ---------------------
                        ArrayList assignmentsS1 = new ArrayList();
                        ArrayList conditionsS1 = new ArrayList();
                        string statement1;

                        assignmentsS1.Add(Models.User.COL_STATUS + "=6");
                        conditionsS1.Add(Models.User.COL_ID + "=" + id);
                        statement1 = QueryGenerator.GenerateSqlUpdate(Models.User.TABLE, assignmentsS1, conditionsS1);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement1.ToString();
                        cmd.ExecuteNonQuery();

                        // Set location for location table -------------
                        ArrayList valuesS2 = new ArrayList();
                        ArrayList assignmentsS2 = new ArrayList();
                        string statement2 = string.Empty;

                        valuesS2.Add(id);
                        valuesS2.Add(lat);
                        valuesS2.Add(lng);
                        valuesS2.Add(QueryGenerator.QuoteString(DateTime.Now.ToString("yyyy-MM-dd hh:mm:ss")));
                        statement2 = QueryGenerator.GenerateSqlInsert(valuesS2, Location.TABLE);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement2;
                        cmd.ExecuteNonQuery();

                        // Get linked users ----------------------------
                        ArrayList columsS3 = new ArrayList();
                        ArrayList conditionsS3 = new ArrayList();
                        string statement3 = string.Empty;

                        columsS3.Add(LinkedUser.COL_ID);
                        columsS3.Add(LinkedUser.COL_USERID1);
                        columsS3.Add(LinkedUser.COL_USERID2);
                        columsS3.Add(LinkedUser.COL_ALERT1);
                        columsS3.Add(LinkedUser.COL_ALERT2);
                        columsS3.Add(LinkedUser.COL_MUTE1);
                        columsS3.Add(LinkedUser.COL_MUTE2);
                        columsS3.Add(LinkedUser.COL_DELETED);
                        columsS3.Add(LinkedUser.COL_ADDED1);
                        columsS3.Add(LinkedUser.COL_ADDED2);
                        conditionsS3.Add(LinkedUser.COL_USERID1 + "=" + id);
                        conditionsS3.Add(QueryGenerator.KW_OR);
                        conditionsS3.Add(LinkedUser.COL_USERID2 + "=" + id);
                        statement3 = QueryGenerator.GenerateSqlSelect(columsS3, LinkedUser.TABLE, conditionsS3);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement3;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                lu = new LinkedUser();
                                int i = 0;
                                lu.ID = dr.GetInt32(i);
                                i++;
                                lu.UserIDMe = dr.GetInt32(i);
                                if (lu.UserIDMe == int.Parse(id))
                                {
                                    lu.UserIDMe = dr.GetInt32(i);
                                    i++;
                                    lu.UserIDTarget = dr.GetInt32(i);
                                    i++;
                                    lu.AlertMe = dr.GetBoolean(i);
                                    i++;
                                    lu.AlertTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteMe = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.Deleted = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedMe = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedTarget = dr.GetBoolean(i);
                                }
                                else
                                {
                                    lu.UserIDTarget = dr.GetInt32(i);
                                    i++;
                                    lu.UserIDMe = dr.GetInt32(i);
                                    i++;
                                    lu.AlertTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.AlertMe = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteMe = dr.GetBoolean(i);
                                    i++;
                                    lu.Deleted = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedMe = dr.GetBoolean(i);
                                }

                                linkedUsers.Add(lu);
                            }
                            dr.Close();
                        }
                        linkedUsers.Sort();

                        // Filter linked users -------------------------
                        for (int i = linkedUsers.Count - 1; i >= 0; --i)
                        {
                            LinkedUser linkedUser = (LinkedUser)linkedUsers[i];
                            if (!(linkedUser.AddedMe && linkedUser.AddedTarget)
                                || !linkedUser.AlertMe
                                || linkedUser.MuteTarget
                                || linkedUser.Deleted)
                            {
                                linkedUsers.RemoveAt(i);
                            }
                        }

                        // Get linked user emails ----------------------
                        ArrayList columsS4 = new ArrayList();
                        ArrayList conditionsS4 = new ArrayList();
                        string statement4 = string.Empty;

                        columsS4.Add(Models.User.COL_DELETED);
                        columsS4.Add(Models.User.COL_EMAIL);
                        for (int i = 0; i < linkedUsers.Count; ++i)
                        {
                            if (i > 0)
                            {
                                conditionsS4.Add(QueryGenerator.KW_OR);
                            }
                            conditionsS4.Add(Models.User.COL_ID + "=" + ((LinkedUser)linkedUsers[i]).UserIDTarget);
                        }
                        statement4 = QueryGenerator.GenerateSqlSelect(columsS4, Models.User.TABLE, conditionsS4);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement4;

                        // Set linked user emails ----------------------
                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                if (!dr.GetBoolean(0))
                                {
                                    if (dr.GetValue(1) != DBNull.Value)
                                    {
                                        emailsToAlert.Add(dr.GetString(1));
                                    }
                                }
                            }
                            dr.Close();
                        }

                        // Get this user's name ------------------------
                        ArrayList columsS5 = new ArrayList();
                        ArrayList conditionsS5 = new ArrayList();
                        string statement5 = string.Empty;

                        columsS5.Add(Models.User.COL_USERNAME);
                        conditionsS5.Add(Models.User.COL_ID + "=" + id);
                        statement5 = QueryGenerator.GenerateSqlSelect(columsS5, Models.User.TABLE, conditionsS5);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement5;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                userName = dr.GetString(0);
                            }
                            dr.Close();
                        }
                    }
                    con.Close();
                }

                // Send notification
                foreach (string email in emailsToAlert)
                {
                    string message = "[" + userName + "] Help!";
                    SendNotification(message, email);
                }
            }
            catch (Exception e)
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", e.ToString()));
            }

            if (emailsToAlert.Count == 0)
                return ResponseMessage(JsonContent.ReturnMessage("No linked user is found.", ""));
            return ResponseMessage(JsonContent.ReturnMessage("Linked users are alerted.", ""));
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/unalert")]
        public IHttpActionResult Unalert([FromBody] object data)
        {
            LinkedUser lu;
            ArrayList linkedUsers = new ArrayList();
            ArrayList emailsToAlert = new ArrayList();
            string userName = string.Empty;

            try
            {
                var headers = Request.Headers;
                string id = headers.GetValues(Models.User.COL_ID).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        // Set flag for user table ---------------------
                        ArrayList assignmentsS1 = new ArrayList();
                        ArrayList conditionsS1 = new ArrayList();
                        string statement1;

                        assignmentsS1.Add(Models.User.COL_STATUS + "=5");
                        conditionsS1.Add(Models.User.COL_ID + "=" + id);
                        statement1 = QueryGenerator.GenerateSqlUpdate(Models.User.TABLE, assignmentsS1, conditionsS1);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement1.ToString();
                        cmd.ExecuteNonQuery();

                        // Get linked users ----------------------------
                        ArrayList columsS2 = new ArrayList();
                        ArrayList conditionsS2 = new ArrayList();
                        string statement2 = string.Empty;

                        columsS2.Add(LinkedUser.COL_ID);
                        columsS2.Add(LinkedUser.COL_USERID1);
                        columsS2.Add(LinkedUser.COL_USERID2);
                        columsS2.Add(LinkedUser.COL_ALERT1);
                        columsS2.Add(LinkedUser.COL_ALERT2);
                        columsS2.Add(LinkedUser.COL_MUTE1);
                        columsS2.Add(LinkedUser.COL_MUTE2);
                        columsS2.Add(LinkedUser.COL_DELETED);
                        columsS2.Add(LinkedUser.COL_ADDED1);
                        columsS2.Add(LinkedUser.COL_ADDED2);
                        conditionsS2.Add(LinkedUser.COL_USERID1 + "=" + id);
                        conditionsS2.Add(QueryGenerator.KW_OR);
                        conditionsS2.Add(LinkedUser.COL_USERID2 + "=" + id);
                        statement2 = QueryGenerator.GenerateSqlSelect(columsS2, LinkedUser.TABLE, conditionsS2);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement2;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                lu = new LinkedUser();
                                int i = 0;
                                lu.ID = dr.GetInt32(i);
                                i++;
                                lu.UserIDMe = dr.GetInt32(i);
                                if (lu.UserIDMe == int.Parse(id))
                                {
                                    lu.UserIDMe = dr.GetInt32(i);
                                    i++;
                                    lu.UserIDTarget = dr.GetInt32(i);
                                    i++;
                                    lu.AlertMe = dr.GetBoolean(i);
                                    i++;
                                    lu.AlertTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteMe = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.Deleted = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedMe = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedTarget = dr.GetBoolean(i);
                                }
                                else
                                {
                                    lu.UserIDTarget = dr.GetInt32(i);
                                    i++;
                                    lu.UserIDMe = dr.GetInt32(i);
                                    i++;
                                    lu.AlertTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.AlertMe = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.MuteMe = dr.GetBoolean(i);
                                    i++;
                                    lu.Deleted = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedTarget = dr.GetBoolean(i);
                                    i++;
                                    lu.AddedMe = dr.GetBoolean(i);
                                }

                                linkedUsers.Add(lu);
                            }
                            dr.Close();
                        }
                        linkedUsers.Sort();

                        // Filter linked users -------------------------
                        for (int i = linkedUsers.Count - 1; i >= 0; --i)
                        {
                            LinkedUser linkedUser = (LinkedUser)linkedUsers[i];
                            if (!(linkedUser.AddedMe && linkedUser.AddedTarget)
                                || !linkedUser.AlertMe
                                || linkedUser.MuteTarget
                                || linkedUser.Deleted)
                            {
                                linkedUsers.RemoveAt(i);
                            }
                        }

                        // Get linked user emails ----------------------
                        ArrayList columsS3 = new ArrayList();
                        ArrayList conditionsS3 = new ArrayList();
                        string statement3 = string.Empty;

                        columsS3.Add(Models.User.COL_DELETED);
                        columsS3.Add(Models.User.COL_EMAIL);
                        for (int i = 0; i < linkedUsers.Count; ++i)
                        {
                            if (i > 0)
                            {
                                conditionsS3.Add(QueryGenerator.KW_OR);
                            }
                            conditionsS3.Add(Models.User.COL_ID + "=" + ((LinkedUser)linkedUsers[i]).UserIDTarget);
                        }
                        statement3 = QueryGenerator.GenerateSqlSelect(columsS3, Models.User.TABLE, conditionsS3);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement3;

                        // Set linked user emails ----------------------
                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                if (!dr.GetBoolean(0))
                                {
                                    if (dr.GetValue(1) != DBNull.Value)
                                    {
                                        emailsToAlert.Add(dr.GetString(1));
                                    }
                                }
                            }
                            dr.Close();
                        }

                        // Get this user's name ------------------------
                        ArrayList columsS4 = new ArrayList();
                        ArrayList conditionsS4 = new ArrayList();
                        string statement4 = string.Empty;

                        columsS4.Add(Models.User.COL_USERNAME);
                        conditionsS4.Add(Models.User.COL_ID + "=" + id);
                        statement4 = QueryGenerator.GenerateSqlSelect(columsS4, Models.User.TABLE, conditionsS4);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement4;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                userName = dr.GetString(0);
                            }
                            dr.Close();
                        }
                    }
                    con.Close();
                }

                // Send notification
                foreach (string email in emailsToAlert)
                {
                    string message = "[" + userName + "] I'm safe now.";
                    SendNotification(message, email);
                }
            }
            catch (Exception e)
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", e.ToString()));
            }

            if (emailsToAlert.Count == 0)
                return ResponseMessage(JsonContent.ReturnMessage("No linked user is found.", ""));
            return ResponseMessage(JsonContent.ReturnMessage("Linked users are unalerted.", ""));
        }

        private bool SendNotification(string message, string suffixReceiverTag)
        {
            string tag = TAG_PREFIX + ":" + suffixReceiverTag;
            string notification = "{ \"data\" : {\"message\":\"" + message + "\"}}";

            try
            {
                Notifications.Instance.Hub.SendGcmNativeNotificationAsync(notification, tag);
            }
            catch (Exception e)
            {
                return false;
            }

            return true;
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/dev/notification/test")]
        public IHttpActionResult TestNotification([FromBody] object data)
        {
            var headers = Request.Headers;
            string user = headers.GetValues("sender").First();
            string message = headers.GetValues("message").First();
            string token = "email:" + headers.GetValues("tag").First();
            string notif = "{ \"data\" : {\"message\":\"" + message + "\"}}";

            try
            {
                Notifications.Instance.Hub.SendGcmNativeNotificationAsync(notif, token);
            }
            catch (Exception e)
            {
                return ResponseMessage(JsonContent.ReturnMessage("Failed to alert.", e.ToString()));
            }

            return ResponseMessage(JsonContent.ReturnMessage("Users are alerted.", ""));
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/dev/notification/test2")]
        public async Task<HttpResponseMessage> TestNotification2([FromBody] object data)
        {
            string pns = "gcm";
            var user = HttpContext.Current.User.Identity.Name;
            string[] userTag = new string[2];
            userTag[0] = "email:" + "test01@email.com";
            userTag[1] = "from:" + user;
            string message = "test";

            Microsoft.Azure.NotificationHubs.NotificationOutcome outcome = null;
            HttpStatusCode ret = HttpStatusCode.InternalServerError;

            switch (pns.ToLower())
            {
                case "wns":
                    // Windows 8.1 / Windows Phone 8.1
                    var toast = @"<toast><visual><binding template=""ToastText01""><text id=""1"">" +
                                "From " + user + ": " + message + "</text></binding></visual></toast>";
                    outcome = await Notifications.Instance.Hub.SendWindowsNativeNotificationAsync(toast, userTag);
                    break;
                case "apns":
                    // iOS
                    var alert = "{\"aps\":{\"alert\":\"" + "From " + user + ": " + message + "\"}}";
                    outcome = await Notifications.Instance.Hub.SendAppleNativeNotificationAsync(alert, userTag);
                    break;
                case "gcm":
                    // Android
                    var notif = "{ \"data\" : {\"message\":\"" + "From " + user + ": " + message + "\"}}";
                    outcome = await Notifications.Instance.Hub.SendGcmNativeNotificationAsync(notif, userTag);
                    break;
            }

            if (outcome != null)
            {
                if (!((outcome.State == Microsoft.Azure.NotificationHubs.NotificationOutcomeState.Abandoned) ||
                    (outcome.State == Microsoft.Azure.NotificationHubs.NotificationOutcomeState.Unknown)))
                {
                    ret = HttpStatusCode.OK;
                }
            }

            return Request.CreateResponse(ret);
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/dev/notification/tags")]
        public async Task<HttpResponseMessage> GetRegisteredTags()
        {
            CollectionQueryResult<RegistrationDescription> outcome = null;
            outcome = await Notifications.Instance.Hub.GetAllRegistrationsAsync(0);

            return Request.CreateResponse(outcome);
        }
    }
}