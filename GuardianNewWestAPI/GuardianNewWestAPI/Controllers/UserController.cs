using System;
using System.Web.Http;
using System.Data.SqlClient;
using System.Data;
using System.Collections;
using System.Linq;
using System.Text;
using System.Web;
using System.Security.Principal;
using GuardianNewWestAPI.Models;
using GuardianNewWestAPI.Utilities;
using GuardianNewWestAPI.Filters;

namespace GuardianNewWestAPI.Controllers
{
    public class UserController : ApiController
    {
        [HttpPost]
        [AcceptVerbs("GET", "POST")]
        [Route("~/user/login")]
        public IHttpActionResult LoginByEmail([FromBody] object data)
        {
            User user = new User();

            try
            {
                var headers = Request.Headers;
                string[] authToken = headers.GetValues("Authorization").First().Split(' ');
                string decodedToken = Encoding.UTF8.GetString(Convert.FromBase64String(authToken[1]));
                string email = decodedToken.Substring(0, decodedToken.IndexOf(":"));
                string password = decodedToken.Substring(decodedToken.IndexOf(":") + 1);

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList assignments = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement;

                        assignments.Add(Models.User.COL_LASTLOGIN
                                        + "=" + QueryGenerator.QuoteString(DateTime.Now.ToString("yyyy-MM-dd hh:mm:ss")));
                        assignments.Add(Models.User.COL_LOGIN + "=1");
                        conditions.Add(Models.User.COL_EMAIL + "=" + QueryGenerator.QuoteString(email));
                        conditions.Add(QueryGenerator.KW_AND);
                        conditions.Add(Models.User.COL_PASSWORD + "=" + QueryGenerator.QuoteString(password));
                        conditions.Add(QueryGenerator.KW_AND);
                        conditions.Add(Models.User.COL_DELETED + "=0");
                        statement = QueryGenerator.GenerateSqlUpdate(Models.User.TABLE, assignments, conditions);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement.ToString();
                        cmd.ExecuteNonQuery();

                        ArrayList columsS2 = new ArrayList();
                        ArrayList conditionsS2 = new ArrayList();
                        string statement2 = string.Empty;
                        columsS2.Add(Models.User.COL_ID);
                        columsS2.Add(Models.User.COL_USERNAME);
                        columsS2.Add(Models.User.COL_EMAIL);
                        columsS2.Add(Models.User.COL_PHONE);
                        columsS2.Add(Models.User.COL_LOGIN);
                        columsS2.Add(Models.User.COL_LASTLOGIN);
                        columsS2.Add(Models.User.COL_STATUS);
                        conditionsS2.Add(Models.User.COL_EMAIL + "=" + QueryGenerator.QuoteString(email));
                        conditionsS2.Add(QueryGenerator.KW_AND);
                        conditionsS2.Add(Models.User.COL_PASSWORD + "=" + QueryGenerator.QuoteString(password));
                        conditionsS2.Add(QueryGenerator.KW_AND);
                        conditionsS2.Add(Models.User.COL_DELETED + "=0");
                        statement2 = QueryGenerator.GenerateSqlSelect(columsS2, Models.User.TABLE, conditionsS2);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement2;

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
            catch
            {
                new System.Net.Http.HttpResponseMessage(System.Net.HttpStatusCode.Unauthorized);
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            if (user.ID == 0)
            {
                new System.Net.Http.HttpResponseMessage(System.Net.HttpStatusCode.Unauthorized);
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            HttpContext.Current.User = new GenericPrincipal(new ApiIdentity(user), new string[] { });
            return Ok(new { user });
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/user/logout")]
        public IHttpActionResult LogoutByEmail([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string email = headers.GetValues(Models.User.COL_EMAIL).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList assignments = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement;

                        assignments.Add(Models.User.COL_LOGIN + "=0");
                        conditions.Add(Models.User.COL_EMAIL + "=" + QueryGenerator.QuoteString(email));
                        statement = QueryGenerator.GenerateSqlUpdate(Models.User.TABLE, assignments, conditions);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement.ToString();
                        cmd.ExecuteNonQuery();
                    }
                    con.Close();
                }
            }
            catch
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            return ResponseMessage(JsonContent.ReturnMessage("The request is processed.", ""));
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/user/get")]
        public IHttpActionResult GetUserByEmail([FromBody] object data)
        {
            User user = new User();

            try
            {
                var headers = Request.Headers;
                string email = headers.GetValues(Models.User.COL_EMAIL).First();

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
                        conditions.Add(Models.User.COL_EMAIL + "=" + QueryGenerator.QuoteString(email));
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
                return ResponseMessage(JsonContent.ReturnMessage("No user is found.", ""));
            return Ok(new { user });
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/user/getbyid")]
        public IHttpActionResult GetUserById([FromBody] object data)
        {
            User user = new User();

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
                return ResponseMessage(JsonContent.ReturnMessage("No user is found.", ""));
            return Ok(new { user });
        }

        [HttpPost]
        [AcceptVerbs("GET", "POST")]
        [Route("~/user/create")]
        public IHttpActionResult CreateUser([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string userName = headers.GetValues(Models.User.COL_USERNAME).First();
                string email = headers.GetValues(Models.User.COL_EMAIL).First();
                string password = headers.GetValues(Models.User.COL_PASSWORD).First();
                string phone = headers.GetValues(Models.User.COL_PHONE).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList values = new ArrayList();
                        string statement = string.Empty;

                        values.Add(QueryGenerator.QuoteString(userName));
                        values.Add(QueryGenerator.QuoteString(email));
                        values.Add(QueryGenerator.QuoteString(password));
                        values.Add(phone);
                        values.Add("0"); // Login
                        values.Add("NULL"); // LastLogin
                        values.Add("1"); // StatusID
                        values.Add("0"); // Deleted
                        statement = QueryGenerator.GenerateSqlInsert(values, Models.User.TABLE);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement;
                        cmd.ExecuteNonQuery();
                    }
                    con.Close();
                }
            }
            catch
            {
                //return ResponseMessage(Request.CreateResponse(HttpStatusCode.BadRequest, false));
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            //return ResponseMessage(Request.CreateResponse(HttpStatusCode.OK, true));
            return ResponseMessage(JsonContent.ReturnMessage("The request is processed.", ""));
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/user/delete")]
        public IHttpActionResult DeleteUserById([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string id = headers.GetValues(Models.User.COL_ID).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList assignments = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement;

                        assignments.Add(Models.User.COL_DELETED + " = 1");
                        conditions.Add(Models.User.COL_ID + " = " + id);
                        statement = QueryGenerator.GenerateSqlUpdate(Models.User.TABLE, assignments, conditions);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement.ToString();
                        cmd.ExecuteNonQuery();
                    }
                    con.Close();
                }
            }
            catch
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            return ResponseMessage(JsonContent.ReturnMessage("The request is processed.", ""));
        }

        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/token/refresh")]
        public IHttpActionResult RefreshTokenById([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string id = headers.GetValues(Models.User.COL_ID).First();
                string token = headers.GetValues(Models.User.COL_TOKEN).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList assignments = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement;

                        assignments.Add(Models.User.COL_TOKEN + "=" + QueryGenerator.QuoteString(token));
                        conditions.Add(Models.User.COL_ID + "=" + id);
                        statement = QueryGenerator.GenerateSqlUpdate(Models.User.TABLE, assignments, conditions);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement.ToString();
                        cmd.ExecuteNonQuery();
                    }
                    con.Close();
                }
            }
            catch
            {
                return ResponseMessage(JsonContent.ReturnMessage("The request is invalid.", ""));
            }

            return ResponseMessage(JsonContent.ReturnMessage("The request is processed.", ""));
        }
    }
}
