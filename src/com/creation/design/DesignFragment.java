package com.creation.design;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.touch.TEstadoDetector;
import com.android.view.OpenGLFragment;
import com.creation.data.Esqueleto;
import com.project.main.GamePreferences;
import com.project.main.R;

public class DesignFragment extends OpenGLFragment
{
	private DesignFragmentListener mCallback;

	private DesignGLSurfaceView canvas;
	private ImageButton botonReset, botonTriangular, botonListo;

	private DesignDataSaved dataSaved;

	/* Constructora */

	public static final DesignFragment newInstance()
	{
		DesignFragment fragment = new DesignFragment();
		return fragment;
	}

	public interface DesignFragmentListener
	{
		public void onDesignReadyButtonClicked(Esqueleto e);
	}

	/* M�todos Fragment */

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mCallback = (DesignFragmentListener) activity;
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		mCallback = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Seleccionar Layout
		View rootView = inflater.inflate(R.layout.fragment_creation_design_layout, container, false);

		// Instanciar Elementos de la GUI
		canvas = (DesignGLSurfaceView) rootView.findViewById(R.id.designGLSurfaceViewDesign1);

		botonListo = (ImageButton) rootView.findViewById(R.id.imageButtonDesign1);
		botonReset = (ImageButton) rootView.findViewById(R.id.imageButtonDesign2);
		botonTriangular = (ImageButton) rootView.findViewById(R.id.imageButtonDesign3);

		botonListo.setOnClickListener(new OnReadyClickListener());
		botonReset.setOnClickListener(new onResetClickListener());
		botonTriangular.setOnClickListener(new onTriangularClickListener());

		setCanvasListener(canvas);

		reiniciarInterfaz();
		actualizarInterfaz();
		
		sendAlertMessage(R.string.text_tip_design_draw_title, R.string.text_tip_design_draw_description, GamePreferences.VIDEO_DESIGN_DRAW_PATH);
		
		return rootView;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		canvas = null;
		botonListo = null;
		botonReset = null;
		botonTriangular = null;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		canvas.onResume();

		if (dataSaved != null)
		{
			canvas.restoreData(dataSaved);

			reiniciarInterfaz();
			actualizarInterfaz();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		canvas.onPause();

		dataSaved = canvas.saveData();
	}

	/* M�todos Abstractos OpenGLFragment */

	@Override
	protected void reiniciarInterfaz()
	{
		botonListo.setVisibility(View.INVISIBLE);
		botonReset.setVisibility(View.INVISIBLE);
		botonTriangular.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void actualizarInterfaz()
	{
		if (canvas.isPoligonoCompleto())
		{
			botonListo.setVisibility(View.VISIBLE);
			botonReset.setVisibility(View.VISIBLE);
			botonTriangular.setVisibility(View.VISIBLE);
		}
	}

	/* M�todos Listener onClick */

	public class OnReadyClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if (canvas.isEstadoDibujando() || canvas.isEstadoTriangulando())
			{
				if (canvas.seleccionarTriangular())
				{
					List<Integer> listaMensajes = new ArrayList<Integer>();
					listaMensajes.add(R.string.text_tip_design_drag_description);
					listaMensajes.add(R.string.text_tip_design_zoom_description);
					listaMensajes.add(R.string.text_tip_design_rotate_description);
					
					List<String> listaVideos = new ArrayList<String>();
					listaVideos.add(GamePreferences.VIDEO_DESIGN_DRAG_PATH);
					listaVideos.add(GamePreferences.VIDEO_DESIGN_ZOOM_PATH);
					listaVideos.add(GamePreferences.VIDEO_DESIGN_ROTATE_PATH);
					
					sendAlertMessage(R.string.text_tip_design_touch_title, listaMensajes, listaVideos);

					canvas.setEstado(TEstadoDetector.CoordDetectors);
					canvas.seleccionarRetoque();
				}
				else
				{
					canvas.setEstado(TEstadoDetector.SimpleTouch);
					
					sendMessage(R.string.text_tip_design_problem_title, R.string.text_tip_design_noregular_description, GamePreferences.VIDEO_DESIGN_NOREGULAR_PATH, R.string.error_triangle);
				}
			}
			else if (canvas.isEstadoRetocando())
			{
				if (canvas.isPoligonoDentroMarco())
				{
					mCallback.onDesignReadyButtonClicked(canvas.getEsqueleto());
				}
				else
				{
					sendMessage(R.string.text_tip_design_problem_title, R.string.text_tip_design_outside_description, GamePreferences.VIDEO_DESIGN_OUTSIDE_PATH, R.string.error_retouch);
				}
			}
		}
	}

	private class onResetClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			canvas.reiniciar();
			canvas.setEstado(TEstadoDetector.SimpleTouch);

			reiniciarInterfaz();
			actualizarInterfaz();
		}
	}

	private class onTriangularClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if (!canvas.seleccionarTriangular())
			{
				sendMessage(R.string.text_tip_design_problem_title, R.string.text_tip_design_noregular_description, GamePreferences.VIDEO_DESIGN_NOREGULAR_PATH, R.string.error_triangle);
			}

			reiniciarInterfaz();
			actualizarInterfaz();
		}
	}
}
