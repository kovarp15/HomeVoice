package cz.kovar.petr.homevoice.frontend.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.UserData;
import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.frontend.views.mjpegView.MjpegView;
import cz.kovar.petr.homevoice.utils.CameraUtils;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.Metrics;
import okhttp3.Cookie;

/**
 * Created by petr on 22.03.17.
 */

public class CameraDialog extends DialogFragment {

    private MjpegView m_mjpegView;
    private Device m_device;

    @Inject
    ApiClient apiClient;

    public CameraDialog() {}

    public static CameraDialog newInstance(Device aDevice) {
        CameraDialog frag = new CameraDialog();
        Bundle args = new Bundle();
        args.putSerializable("device", aDevice);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ZWayApplication) getActivity().getApplicationContext()).getComponent().inject(this);

        m_mjpegView = (MjpegView) view.findViewById(R.id.video_mjpeg_view);
        m_device = (Device) getArguments().getSerializable("device");

        m_mjpegView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        m_mjpegView.showFps(true);
        prepareHeaders();

        Metrics cameraMetrics = m_device.metrics;
        changeButtonVisibility(view.findViewById(R.id.video_btn_up), cameraMetrics.hasUp);
        changeButtonVisibility(view.findViewById(R.id.video_btn_down), cameraMetrics.hasDown);
        changeButtonVisibility(view.findViewById(R.id.video_btn_left), cameraMetrics.hasLeft);
        changeButtonVisibility(view.findViewById(R.id.video_btn_right), cameraMetrics.hasRight);
        changeButtonVisibility(view.findViewById(R.id.video_btn_zoom_in), cameraMetrics.hasZoomIn);
        changeButtonVisibility(view.findViewById(R.id.video_btn_zoom_out), cameraMetrics.hasZoomOut);
        changeButtonVisibility(view.findViewById(R.id.video_btn_open), cameraMetrics.hasOpen);
        changeButtonVisibility(view.findViewById(R.id.video_btn_close), cameraMetrics.hasClose);

    }

    @Override
    public void onResume() {
        super.onResume();
        final String cameraUrl = CameraUtils.getCameraUrl(UserData.loadZWayProfile(getContext()),
                m_device.metrics.url);
        m_mjpegView.setSource(cameraUrl);
    }

    @Override
    public void onPause() {
        super.onPause();
        m_mjpegView.stopPlayback();
    }

    private void changeButtonVisibility(View v, boolean isVisible) {
        v.setVisibility(isVisible ? View.VISIBLE :View.INVISIBLE);
    }

    private void prepareHeaders() {
        final Cookie cookie = apiClient.getCookie();
        if(cookie != null && !TextUtils.isEmpty(cookie.value())) {
            m_mjpegView.addHeader(cookie.name(), cookie.value());
        }
    }
}
