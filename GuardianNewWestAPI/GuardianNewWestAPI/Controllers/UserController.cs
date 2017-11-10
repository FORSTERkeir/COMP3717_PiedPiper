using Swashbuckle.Swagger.Annotations;
using System;
using System.Collections.Generic;
using System.Net;
using System.Web.Http;
using System.Data.SqlClient;
using System.Data;
using System.Collections;
using Utilities.QueryGenerator;
using Utilities.JsonContent;
using Filters.BasicAuthenticationAttribute;
using System.Linq;
using Models.User;
using Newtonsoft.Json.Linq;

namespace Models.Controllers
{
    public class UserController : ApiController
    {
        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [SwaggerResponse(HttpStatusCode.OK,
            Description = "OK",
            Type = typeof(IEnumerable<User.User>))]
        [SwaggerResponse(HttpStatusCode.NotFound,
            Description = "User not found",
            Type = typeof(IEnumerable<User.User>))]
        [SwaggerOperation("GetUserByEmail")]
        [Route("~/user/get")]
        public IHttpActionResult GetUserByEmail([FromBody] object data)
        {
            User.User user = new User.User();

            try
            {
                var headers = Request.Headers;
                string email = headers.GetValues(Models.User.User.COL_EMAIL).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList colums = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement = string.Empty;

                        colums.Add(Models.User.User.COL_ID);
                        colums.Add(Models.User.User.COL_USERNAME);
                        colums.Add(Models.User.User.COL_EMAIL);
                        colums.Add(Models.User.User.COL_PHONE);
                        colums.Add(Models.User.User.COL_LOGIN);
                        colums.Add(Models.User.User.COL_LASTLOGIN);
                        colums.Add(Models.User.User.COL_STATUS);
                        conditions.Add(Models.User.User.COL_EMAIL + " = " + QueryGenerator.QuoteString(email));
                        conditions.Add(QueryGenerator.KW_AND);
                        conditions.Add(Models.User.User.COL_DELETED + " = 0");
                        statement = QueryGenerator.GenerateSqlSelect(colums, Models.User.User.TABLE, conditions);

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

            if (user.UserName == null)
                return ResponseMessage(JsonContent.ReturnMessage("No user is found.", ""));
            return Ok(new { user });
        }

        [HttpPost]
        [AcceptVerbs("GET", "POST")]
        [SwaggerResponse(HttpStatusCode.Created,
            Description = "Created",
            Type = typeof(bool))]
        [SwaggerOperation("CreateUser")]
        [Route("~/user/create")]
        public IHttpActionResult CreateUser([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string userName = headers.GetValues(Models.User.User.COL_USERNAME).First();
                string email = headers.GetValues(Models.User.User.COL_EMAIL).First();
                string password = headers.GetValues(Models.User.User.COL_PASSWORD).First();
                string phone = headers.GetValues(Models.User.User.COL_PHONE).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList values = new ArrayList();
                        ArrayList assignments = new ArrayList();
                        string statement = string.Empty;

                        values.Add(QueryGenerator.QuoteString(userName));
                        values.Add(QueryGenerator.QuoteString(email));
                        values.Add(QueryGenerator.QuoteString(password));
                        values.Add(phone);
                        values.Add("0"); // Login
                        values.Add("NULL"); // LastLogin
                        values.Add("1"); // StatusID
                        values.Add("0"); // Deleted
                        statement = QueryGenerator.GenerateSqlInsert(values, Models.User.User.TABLE);

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
        [SwaggerResponse(HttpStatusCode.OK,
            Description = "OK",
            Type = typeof(bool))]
        [SwaggerResponse(HttpStatusCode.NotFound,
            Description = "Account not found",
            Type = typeof(bool))]
        [SwaggerOperation("DeleteUserByEmail")]
        [Route("~/user/delete")]
        public IHttpActionResult DeleteUserByEmail([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string email = headers.GetValues(Models.User.User.COL_EMAIL).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList assignments = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement;

                        assignments.Add(Models.User.User.COL_DELETED + " = 1");
                        conditions.Add(Models.User.User.COL_EMAIL + " = " + QueryGenerator.QuoteString(email));
                        statement = QueryGenerator.GenerateSqlUpdate(Models.User.User.TABLE, assignments, conditions);

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
