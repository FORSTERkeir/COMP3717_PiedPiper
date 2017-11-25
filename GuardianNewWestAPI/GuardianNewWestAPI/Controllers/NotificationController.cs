using System;
using System.Web.Http;
using System.Data.SqlClient;
using System.Data;
using System.Collections;
using System.Linq;
using GuardianNewWestAPI.Models;
using GuardianNewWestAPI.Utilities;
using GuardianNewWestAPI.Filters;

namespace GuardianNewWestAPI.Controllers
{
    public class NotificationController : ApiController
    {
        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/alert")]
        public IHttpActionResult Alert([FromBody] object data)
        {
            User user = new User();
            ArrayList notifyKeys = new ArrayList();

            try
            {
                var headers = Request.Headers;
                string id = headers.GetValues(Models.User.COL_ID).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList colums = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement = string.Empty;

                        colums.Add(Models.User.COL_ID);
                        colums.Add(Models.User.COL_USERNAME);
                        colums.Add(Models.User.COL_EMAIL);
                        colums.Add(Models.User.COL_PHONE);
                        colums.Add(Models.User.COL_LOGIN);
                        colums.Add(Models.User.COL_LASTLOGIN);
                        colums.Add(Models.User.COL_STATUS);
                        conditions.Add(Models.User.COL_ID + "=" + id);
                        conditions.Add(QueryGenerator.KW_AND);
                        conditions.Add(Models.User.COL_DELETED + "=0");
                        statement = QueryGenerator.GenerateSqlSelect(colums, Models.User.TABLE, conditions);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                user.ID = dr.GetInt32(0);
                                user.UserName = dr.GetString(1);
                                user.Email = dr.GetString(2);
                                user.Phone = dr.GetInt64(3);
                                user.Login = dr.GetBoolean(4);
                                if (dr.GetValue(5) != DBNull.Value)
                                    user.LastLogin = dr.GetDateTime(5);
                                user.Status = dr.GetInt32(6);
                            }
                            dr.Close();
                        }
                    }
                    con.Close();
                }
            }
            catch (Exception e)
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            if (user.ID == 0)
                return ResponseMessage(JsonContent.ReturnMessage("No linked user is found.", ""));
            return ResponseMessage(JsonContent.ReturnMessage("Linked users are alerted.", ""));
        }
    }
}
