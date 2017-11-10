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
    public class EmergencyBldgController : ApiController
    {
        [HttpGet]
        [BasicAuthentication]
        [AcceptVerbs("GET")]
        [SwaggerResponse(HttpStatusCode.OK,
            Description = "OK",
            Type = typeof(IEnumerable<User.User>))]
        [SwaggerResponse(HttpStatusCode.NotFound,
            Description = "Emergency building not found",
            Type = typeof(IEnumerable<User.User>))]
        [SwaggerOperation("GetEmergencyBldgAll")]
        [Route("~/emergencybldg/get/all")]
        public IHttpActionResult GetEmergencyBldgAll()
        {
            ArrayList buildings = new ArrayList();
            EmergencyBldg.EmergencyBldg bldg;

            try
            {
                //var headers = Request.Headers;
                //string email = headers.GetValues(Models.User.User.COL_EMAIL).First();

                using (SqlConnection con = new SqlConnection(QueryGenerator.ConnectionString()))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        ArrayList colums = new ArrayList();
                        ArrayList conditions = new ArrayList();
                        string statement = string.Empty;

                        colums.Add(EmergencyBldg.EmergencyBldg.COL_ID);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_CATEGORY);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_BLDGID);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_BLDGNAME);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_STRNUM);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_STRNAME);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_MAPREF);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_LAT);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_LNG);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_PHONE);
                        colums.Add(EmergencyBldg.EmergencyBldg.COL_LOCNAME);
                        statement = QueryGenerator.GenerateSqlSelect(colums, EmergencyBldg.EmergencyBldg.TABLE, conditions);

                        cmd.CommandType = CommandType.Text;
                        cmd.CommandText = statement;

                        using (SqlDataReader dr = cmd.ExecuteReader())
                        {
                            while (dr.Read())
                            {
                                bldg = new EmergencyBldg.EmergencyBldg();
                                int i = 0;
                                bldg.ID = dr.GetInt32(i);
                                i++;
                                bldg.Category = dr.GetInt32(i);
                                i++;
                                bldg.BldgID = dr.GetInt32(i);
                                i++;
                                bldg.BldgName = dr.GetString(i);
                                i++;
                                bldg.StrNum = dr.GetInt32(i);
                                i++;
                                bldg.StrName = dr.GetString(i);
                                i++;
                                bldg.MapRef = dr.GetInt32(i);
                                i++;
                                bldg.Lat = dr.GetDouble(i);
                                i++;
                                bldg.Lng = dr.GetDouble(i);
                                i++;
                                //if (dr.GetValue(i) != DBNull.Value)
                                //    bldg.Phone = dr.GetInt64(i);
                                //i++;
                                //if (dr.GetValue(i) != DBNull.Value)
                                //    bldg.LocName = dr.GetString(i);
                                buildings.Add(bldg);
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

            if (buildings.Count == 0)
                return ResponseMessage(JsonContent.ReturnMessage("No building is found.", ""));
            return Ok(new { buildings });
        }
    }
}
