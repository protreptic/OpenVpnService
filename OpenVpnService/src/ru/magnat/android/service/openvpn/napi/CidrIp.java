package ru.magnat.android.service.openvpn.napi;

import java.util.Locale;

public class CidrIp {
	
    private String mIpAddress;
    private int mPrefix;
    
    public CidrIp(String ip, String mask) {
        mIpAddress = ip;
        long netmask = getInt(mask);

        // Add 33. bit to ensure the loop terminates
        netmask += 1l << 32;

        int lenZeros = 0;
        while ((netmask & 0x1) == 0) {
            lenZeros++;
            netmask = netmask >> 1;
        }
        // Check if rest of netmask is only 1s
        if (netmask != (0x1ffffffffl >> lenZeros)) {
            // Asume no CIDR, set /32
            mPrefix = 32;
        } else {
            mPrefix = 32 - lenZeros;
        }
    }

    public CidrIp(String address, int prefix) {
    	mIpAddress = address;
    	mPrefix = prefix;
    }

    public void setIp(String address) {
    	mIpAddress = address;
    }
    
    public String getIp() {
    	return mIpAddress;
    }
    
    public void setPrefix(int prefix) {
    	mPrefix = prefix;
    }
    
    public int getPrefix() {
    	return mPrefix;
    }
    
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%s/%d", mIpAddress, mPrefix);
    }

    public boolean normalise() {
        long ip = getInt(mIpAddress);

        long newip = ip & (0xffffffffl << (32 - mPrefix));
        if (newip != ip) {
            mIpAddress = String.format("%d.%d.%d.%d", (newip & 0xff000000) >> 24, (newip & 0xff0000) >> 16, (newip & 0xff00) >> 8, newip & 0xff);
            return true;
        } else {
            return false;
        }
    }

    static long getInt(String ipaddr) {
        String[] ipt = ipaddr.split("\\.");
        long ip = 0;

        ip += Long.parseLong(ipt[0]) << 24;
        ip += Integer.parseInt(ipt[1]) << 16;
        ip += Integer.parseInt(ipt[2]) << 8;
        ip += Integer.parseInt(ipt[3]);

        return ip;
    }

    public long getInt() {
        return getInt(mIpAddress);
    }

}