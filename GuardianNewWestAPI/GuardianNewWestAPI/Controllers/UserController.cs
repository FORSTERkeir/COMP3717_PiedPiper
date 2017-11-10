using UserList.Models;
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
using Newtonsoft.Json.Linq;

namespace UserList.Controllers
{
    public class UserController : ApiController
    {
        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [SwaggerResponse(HttpStatusCode.OK,
            Description = "OK",
            Type = typeof(IEnumerable<User>))]
        [SwaggerResponse(HttpStatusCode.NotFound,
            Description = "User not found",
            Type = typeof(IEnumerable<User>))]
        [SwaggerOperation("GetUserByEmail")]
        [Route("~/user/get")]
        public IHttpActionResult GetUserByEmail([FromBody] object data)
        {
            User user = new User();

            try
            {
                var headers = Request.Headers;
                string email = headers.GetValues("email").First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList colums = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement = string.Empty;

                        colums.Add("UserName");
                        colums.Add("Email");
                        colums.Add("Phone");
                        colums.Add("Login");
                        colums.Add("LastLogin");
                        colums.Add("StatusTypeID");
                        conditions.Add("Email = " + QueryGenerator.QuoteString(email));
                        conditions.Add(QueryGenerator.And());
                        conditions.Add("Del = 0");
                        statement = QueryGenerator.GenerateSqlSelect(colums, QueryGenerator.UserTable(), conditions);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                user.UserName = dr.GetString(0);
                                user.Email = dr.GetString(1);
                                user.Phone = dr.GetInt64(2);
                                user.Login = dr.GetBoolean(3);
                                if (dr.GetValue(4) != DBNull.Value)
                                    user.LastLogin = dr.GetDateTime(4);
                                user.Status = dr.GetInt32(5);
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
                string userName = headers.GetValues("userName").First();
                string email = headers.GetValues("email").First();
                string password = headers.GetValues("password").First();
                string phone = headers.GetValues("phone").First();

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
                        statement = QueryGenerator.GenerateSqlInsert(values, QueryGenerator.UserTable());

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
                string email = headers.GetValues("email").First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList assignments = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement;

                        assignments.Add("Del = 1");
                        conditions.Add("Email = " + QueryGenerator.QuoteString(email));
                        statement = QueryGenerator.GenerateSqlUpdate(QueryGenerator.UserTable(), assignments, conditions);

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
