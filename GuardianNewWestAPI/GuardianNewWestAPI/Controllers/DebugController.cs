using Filters.BasicAuthenticationAttribute;
using Swashbuckle.Swagger.Annotations;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace GuardianNewWestAPI.Controllers
{
    public class DebugController : ApiController
    {
        [HttpPost]
        [AcceptVerbs("GET", "POST")]
        [SwaggerResponse(HttpStatusCode.OK,
            Description = "OK",
            Type = typeof(IEnumerable<string>))]
        [SwaggerOperation("ReturnInput")]
        [Route("~/debug/input")]
        public IHttpActionResult ReturnInput([FromBody] object data)
        {
            return Ok(data.ToString());
        }

    }
}
