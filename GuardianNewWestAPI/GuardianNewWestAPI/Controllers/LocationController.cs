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

namespace Models
{
    public class LocationController : ApiController
    {
        [HttpPost]
        [BasicAuthentication]
        [AcceptVerbs("GET", "POST")]
        [Route("~/location/get")]
        public IHttpActionResult GetLocationByEmail([FromBody] object data)
        {
            Location.Location location = new Location.Location();

            try
            {
                var headers = Request.Headers;
                string email = headers.GetValues(Models.User.User.COL_EMAIL).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList innerColums = new ArrayList();
                        ArrayList innerConditions = new ArrayList();
                        string innerSelect = string.Empty;

                        innerColums.Add(Models.User.User.COL_ID);
                        innerConditions.Add(Models.User.User.COL_EMAIL
                                            + " = "
                                            + QueryGenerator.QuoteString(email));
                        innerSelect = QueryGenerator.GenerateSqlSelect(innerColums,
                                                                       Models.User.User.TABLE,
                                                                       innerConditions,
                                                                       null,
                                                                       QueryGenerator.KW_ASC,
                                                                       1);

                        ArrayList colums = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        ArrayList orders = new ArrayList();
                        string statement = string.Empty;

                        colums.Add(Location.Location.COL_ID);
                        colums.Add(Location.Location.COL_USERID);
                        colums.Add(Location.Location.COL_LAT);
                        colums.Add(Location.Location.COL_LNG);
                        colums.Add(Location.Location.COL_ALERTTIME);
                        conditions.Add(Location.Location.COL_USERID
                                       + " = "
                                       + QueryGenerator.ParenthesisString(innerSelect));
                        orders.Add(Location.Location.COL_ALERTTIME);
                        statement = QueryGenerator.GenerateSqlSelect(colums,
                                                                     Location.Location.TABLE,
                                                                     conditions,
                                                                     orders,
                                                                     QueryGenerator.KW_DSC,
                                                                     1);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                int i = 0;
                                location.ID = dr.GetInt32(i);
                                i++;
                                location.UserID = dr.GetInt32(i);
                                i++;
                                location.Lat = dr.GetDouble(i);
                                i++;
                                location.Lng = dr.GetDouble(i);
                                i++;
                                location.AlertTime = dr.GetDateTime(i);
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

            if (location.ID == 0)
                return ResponseMessage(JsonContent.ReturnMessage("No location is found.", ""));
            return Ok(new { location });
        }

        [HttpPost]
        [AcceptVerbs("GET", "POST")]
        [Route("~/location/create")]
        public IHttpActionResult CreateLocation([FromBody] object data)
        {
            try
            {
                var headers = Request.Headers;
                string email = headers.GetValues(Models.User.User.COL_EMAIL).First();
                string lat = headers.GetValues(Location.Location.COL_LAT).First();
                string lng = headers.GetValues(Location.Location.COL_LNG).First();
                string time = headers.GetValues(Location.Location.COL_ALERTTIME).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList innerColums = new ArrayList();
                        ArrayList innerConditions = new ArrayList();
                        string innerSelect = string.Empty;

                        innerColums.Add(Models.User.User.COL_ID);
                        innerConditions.Add(Models.User.User.COL_EMAIL
                                            + " = "
                                            + QueryGenerator.QuoteString(email));
                        innerSelect = QueryGenerator.GenerateSqlSelect(innerColums,
                                                                       Models.User.User.TABLE,
                                                                       innerConditions,
                                                                       null,
                                                                       QueryGenerator.KW_ASC,
                                                                       1);

                        ArrayList values = new ArrayList();
                        ArrayList assignments = new ArrayList();
                        string statement = string.Empty;

                        values.Add(QueryGenerator.ParenthesisString(innerSelect));
                        values.Add(lat);
                        values.Add(lng);
                        values.Add(QueryGenerator.QuoteString(time));
                        statement = QueryGenerator.GenerateSqlInsert(values, Location.Location.TABLE);

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
    }
}