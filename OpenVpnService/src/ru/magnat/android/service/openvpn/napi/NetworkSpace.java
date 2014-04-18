package ru.magnat.android.service.openvpn.napi;

import android.os.Build;
import android.text.TextUtils;
import junit.framework.Assert;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.util.*;

import ru.magnat.android.service.openvpn.BuildConfig;

public class NetworkSpace {

    static class IpAddress implements Comparable<IpAddress> {
        
    	public int networkMask;
    	
    	private BigInteger mNetAddress;
        private boolean included;
        private boolean isV4;
        private BigInteger mFirstAddress;
        private BigInteger mLastAddress;

        @Override
        public int compareTo(IpAddress another) {
            int comp = getFirstAddress().compareTo(another.getFirstAddress());
            if (comp != 0)
                return comp;

            // bigger mask means smaller address block
            if (networkMask > another.networkMask)
                return -1;
            else if (another.networkMask == networkMask)
                return 0;
            else
                return 1;
        }

        public IpAddress(CidrIp ip, boolean include) {
        	mNetAddress = BigInteger.valueOf(ip.getInt());
        	
            included = include;
            
            networkMask = ip.getPrefix();
            isV4 = true;
        }

        public IpAddress(Inet6Address address, int mask, boolean include) {
            networkMask = mask;
            included = include;

            int s = 128;

            mNetAddress = BigInteger.ZERO;
            for (byte b : address.getAddress()) {
                s -= 16;
                mNetAddress = mNetAddress.add(BigInteger.valueOf(b).shiftLeft(s));
            }
        }

        public BigInteger getLastAddress() {
            if(mLastAddress == null) {
                mLastAddress = getMaskedAddress(true);
            }
            
            return mLastAddress;
        }


        public BigInteger getFirstAddress() {
            if (mFirstAddress ==null)
                mFirstAddress =getMaskedAddress(false);
            return mFirstAddress;
        }

        private BigInteger getMaskedAddress(boolean one) {
            BigInteger numAddress = mNetAddress;

            int numBits;
            if (isV4) {
                numBits = 32 - networkMask;
            } else {
                numBits = 128 - networkMask;
            }

            for (int i = 0; i < numBits; i++) {
                if (one)
                    numAddress = numAddress.setBit(i);
                else
                    numAddress = numAddress.clearBit(i);
            }
            return numAddress;
        }


        @Override
        public String toString() {
            //String in = included ? "+" : "-";
            if (isV4)
                return String.format(Locale.US,"%s/%d", getIPv4Address(), networkMask);
            else
                return String.format(Locale.US, "%s/%d", getIPv6Address(), networkMask);
        }

        IpAddress(BigInteger baseAddress, int mask, boolean included, boolean isV4) {
            this.mNetAddress = baseAddress;
            this.networkMask = mask;
            this.included = included;
            this.isV4 = isV4;
        }


        public IpAddress[] split() {
            IpAddress firsthalf = new IpAddress(getFirstAddress(), networkMask + 1, included, isV4);
            IpAddress secondhalf = new IpAddress(firsthalf.getLastAddress().add(BigInteger.ONE), networkMask + 1, included, isV4);
            if (BuildConfig.DEBUG) Assert.assertTrue(secondhalf.getLastAddress().equals(getLastAddress()));
            return new IpAddress[]{firsthalf, secondhalf};
        }

        String getIPv4Address() {
            if (BuildConfig.DEBUG) {
                Assert.assertTrue (isV4);
                Assert.assertTrue (mNetAddress.longValue() <= 0xffffffffl);
                Assert.assertTrue (mNetAddress.longValue() >= 0);
            }
            long ip = mNetAddress.longValue();
            return String.format(Locale.US, "%d.%d.%d.%d", (ip >> 24) % 256, (ip >> 16) % 256, (ip >> 8) % 256, ip % 256);
        }

        String getIPv6Address() {
            if (BuildConfig.DEBUG) Assert.assertTrue (!isV4);
            BigInteger r = mNetAddress;
            if (r.longValue() == 0)
                return "::";

            Vector<String> parts = new Vector<String>();
            while (r.compareTo(BigInteger.ZERO) == 1) {
                parts.add(0, String.format(Locale.US, "%x", r.mod(BigInteger.valueOf(256)).longValue()));
                r = r.shiftRight(16);
            }

            return TextUtils.join(":", parts);
        }

        public boolean containsNet(IpAddress network) {
            return getFirstAddress().compareTo(network.getFirstAddress()) != 1 &&
                    getLastAddress().compareTo(network.getLastAddress()) != -1;
        }
    }


    TreeSet<IpAddress> mIpAddresses = new TreeSet<IpAddress>();


    public Collection<IpAddress> getNetworks(boolean included) {
        Vector<IpAddress> ips = new Vector<IpAddress>();
        for (IpAddress ip : mIpAddresses) {
            if (ip.included == included)
                ips.add(ip);
        }
        return ips;
    }

    public void clear() {
        mIpAddresses.clear();
    }


    void addIP(CidrIp cidrIp, boolean include) {

        mIpAddresses.add(new IpAddress(cidrIp, include));
    }

    void addIPv6(Inet6Address address, int mask, boolean included) {
        mIpAddresses.add(new IpAddress(address, mask, included));
    }

    TreeSet<IpAddress> generateIPList() {

        PriorityQueue<IpAddress> networks = new PriorityQueue<IpAddress>(mIpAddresses);

        TreeSet<IpAddress> ipsDone = new TreeSet<IpAddress>();

        IpAddress currentNet =  networks.poll();
        if (currentNet==null)
            return ipsDone;

        while (currentNet!=null) {
            // Check if it and the next of it are compatbile
            IpAddress nextNet = networks.poll();

            if (BuildConfig.DEBUG) Assert.assertNotNull(currentNet);
            if (nextNet== null || currentNet.getLastAddress().compareTo(nextNet.getFirstAddress()) == -1) {
                // Everything good, no overlapping nothing to do
                ipsDone.add(currentNet);

                currentNet = nextNet;
            } else {
                // This network is smaller or equal to the next but has the same base address
                if (currentNet.getFirstAddress().equals(nextNet.getFirstAddress()) && currentNet.networkMask >= nextNet.networkMask) {
                    if (currentNet.included == nextNet.included) {
                        // Included in the next next and same type
                        // Simply forget our current network
                        currentNet=nextNet;
                    } else {
                        // our currentnet is included in next and types differ. Need to split the next network
                        IpAddress[] newNets = nextNet.split();

                        // First add the second half to keep the order in networks
                        if (!networks.contains(newNets[1]))
                            networks.add(newNets[1]);

                        if (newNets[0].getLastAddress().equals(currentNet.getLastAddress())) {
                            if (BuildConfig.DEBUG) Assert.assertEquals (newNets[0].networkMask, currentNet.networkMask);
                            // Don't add the lower half that would conflict with currentNet
                        } else {
                            if (!networks.contains(newNets[0]))
                                networks.add(newNets[0]);
                        }
                        // Keep currentNet as is
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        Assert.assertTrue(currentNet.networkMask < nextNet.networkMask);
                        Assert.assertTrue (nextNet.getFirstAddress().compareTo(currentNet.getFirstAddress()) == 1);
                        Assert.assertTrue (currentNet.getLastAddress().compareTo(nextNet.getLastAddress()) != -1);
                    }
                    // This network is bigger than the next and last ip of current >= next

                    if (currentNet.included == nextNet.included) {
                        // Next network is in included in our network with the same type,
                        // simply ignore the next and move on
                    } else {
                        // We need to split our network
                        IpAddress[] newNets = currentNet.split();


                        if (newNets[1].networkMask == nextNet.networkMask) {
                            if (BuildConfig.DEBUG) {
                                Assert.assertTrue (newNets[1].getFirstAddress().equals(nextNet.getFirstAddress()));
                                Assert.assertTrue (newNets[1].getLastAddress().equals(currentNet.getLastAddress()));
                                // Splitted second equal the next network, do not add it
                            }
                            networks.add(nextNet);
                        } else {
                            // Add the smaller network first
                            networks.add(newNets[1]);
                            networks.add(nextNet);
                        }
                        currentNet = newNets[0];

                    }
                }
            }

        }

        return ipsDone;
    }

    Collection<IpAddress> getPositiveIPList() {
        TreeSet<IpAddress> ipsSorted = generateIPList();

        Vector<IpAddress> ips = new Vector<IpAddress>();
        for (IpAddress ia : ipsSorted) {
            if (ia.included)
                ips.add(ia);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // Include postive routes from the original set under < 4.4 since these might overrule the local
            // network but only if no smaller negative route exists
            for(IpAddress origIp: mIpAddresses){
                if (!origIp.included)
                    continue;

                // The netspace exists
                if(ipsSorted.contains(origIp))
                    continue;

                boolean skipIp=false;
                // If there is any smaller net that is excluded we may not add the positive route back
                for (IpAddress calculatedIp: ipsSorted) {
                    if(!calculatedIp.included && origIp.containsNet(calculatedIp)) {
                        skipIp=true;
                        break;
                    }
                }
                if (skipIp)
                    continue;

                // It is safe to include the IP
                ips.add(origIp);
            }

        }

        return ips;
    }

}
