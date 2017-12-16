import play.http.DefaultHttpFilters;

import javax.inject.Inject;

public class Filters extends DefaultHttpFilters {
  @Inject
  public Filters(play.filters.csrf.CSRFFilter csrfFilter,
                 play.filters.headers.SecurityHeadersFilter securityHeadersFilter,
//                 play.filters.hosts.AllowedHostsFilter allowedHostsFilter,
                 config.LoggingFilter loggingFilter) {
    super(csrfFilter, securityHeadersFilter, /*allowedHostsFilter, */loggingFilter);
  }
}