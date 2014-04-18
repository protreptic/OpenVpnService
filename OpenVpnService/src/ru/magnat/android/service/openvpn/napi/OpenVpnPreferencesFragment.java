package ru.magnat.android.service.openvpn.napi;

import ru.magnat.android.service.openvpn.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public abstract class OpenVpnPreferencesFragment extends PreferenceFragment {
	
	protected VpnProfile mProfile;

	protected abstract void loadSettings();
	protected abstract void saveSettings();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String profileUUID = getArguments().getString(getActivity().getPackageName() + ".profileUUID");
		mProfile = ProfileManager.get(getActivity(),profileUUID);
		getActivity().setTitle(getString(R.string.edit_profile_title, mProfile.getName()));

	}
	
	@Override
	public void onPause() {
		super.onPause();
		saveSettings();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState!=null) {
			String profileUUID=savedInstanceState.getString(VpnProfile.EXTRA_PROFILEUUID);
			mProfile = ProfileManager.get(getActivity(),profileUUID);
			loadSettings();
		}
	}

    @Override
	public void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		saveSettings();
		outState.putString(VpnProfile.EXTRA_PROFILEUUID, mProfile.getUUIDString());
	}
}
