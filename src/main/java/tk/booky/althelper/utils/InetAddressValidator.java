package tk.booky.althelper.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InetAddressValidator implements Serializable {

    private static final int IPV4_MAX_OCTET_VALUE = 255;
    private static final String IPV4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
    private static final InetAddressValidator VALIDATOR = new InetAddressValidator();
    private final RegexValidator ipv4Validator = new RegexValidator(IPV4_REGEX);

    public static InetAddressValidator getInstance() {
        return VALIDATOR;
    }

    public boolean isValidInet4Address(String inet4Address) {
        String[] groups = ipv4Validator.match(inet4Address);
        if (groups == null) return false;

        for (String ipSegment : groups) {
            if (ipSegment == null || ipSegment.length() == 0) return false;
            int iIpSegment;

            try {
                iIpSegment = Integer.parseInt(ipSegment);
            } catch (NumberFormatException exception) {
                return false;
            }

            if (iIpSegment > IPV4_MAX_OCTET_VALUE) return false;
            else if (ipSegment.length() > 1 && ipSegment.startsWith("0")) return false;
        }

        return true;
    }
}