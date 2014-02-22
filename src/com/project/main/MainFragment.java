package com.project.main;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.project.data.Personaje;
import com.view.display.DisplayGLSurfaceView;

public class MainFragment extends OpenGLFragment
{
	private MainFragmentListener mCallback;
	
	private DisplayGLSurfaceView canvas;
	private ImageButton botonCrear, botonJugar, botonSeleccionar, botonTest;
	
	private List<Personaje> listaPersonajes;
	private int personajeSeleccionado;
	
	/* SECTION Constructora */
	
	public static final MainFragment newInstance(List<Personaje> lista, int indice)
	{
		MainFragment fragment = new MainFragment();
		fragment.setParameters(lista, indice);
		return fragment;
	}
	
	private void setParameters(List<Personaje> lista, int indice)
	{
		listaPersonajes = lista;
		personajeSeleccionado = indice;
	}
	
	public interface MainFragmentListener
	{
		public void onMainCreateButtonClicked();
		public void onMainSelectButtonClicked();
		public void onMainTestButtonClicked();
		public void onMainPlayButtonClicked();
    }
	
	/* SECTION M�todos Fragment */
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mCallback = (MainFragmentListener) activity;
	}
	
	@Override
	public void onDetach()
	{
		super.onDetach();
		
		mCallback = null;
		listaPersonajes = null;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{		
		// Seleccionar Layout
		View rootView = inflater.inflate(R.layout.fragment_main_layout, container, false);
		
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.removeAllTabs();
		
		// Instanciar Elementos de la GUI
		canvas = (DisplayGLSurfaceView) rootView.findViewById(R.id.displayGLSurfaceViewMain1);
		if(personajeSeleccionado >= 0 && personajeSeleccionado < listaPersonajes.size() && !listaPersonajes.isEmpty())
		{
			Personaje p = listaPersonajes.get(personajeSeleccionado);
			canvas.setParameters(p);
		}
		else
		{
			canvas.setParameters();
		}
		
		botonCrear = (ImageButton) rootView.findViewById(R.id.imageButtonMain1);
		botonTest = (ImageButton) rootView.findViewById(R.id.imageButtonMain2);
		botonSeleccionar = (ImageButton) rootView.findViewById(R.id.imageButtonMain3);
		botonJugar = (ImageButton) rootView.findViewById(R.id.imageButtonMain4);
		
		botonCrear.setOnClickListener(new OnAddClickListener());
		botonSeleccionar.setOnClickListener(new OnViewClickListener());
		botonTest.setOnClickListener(new OnTestClickListener());
		botonJugar.setOnClickListener(new OnGameClickListener());
		
		reiniciarInterfaz();
		actualizarInterfaz();
        return rootView;
    }
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		
		canvas = null;
		botonCrear = null;
		botonJugar = null;
		botonSeleccionar = null;
		botonTest = null;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		canvas.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		canvas.saveData();
		canvas.onPause();
	}
	
	/* SECTION M�todos abstractos de OpenGLFragment */
	
	@Override
	protected void reiniciarInterfaz()
	{
		botonSeleccionar.setVisibility(View.INVISIBLE);	
	}

	@Override
	protected void actualizarInterfaz()
	{
		if(!listaPersonajes.isEmpty())
		{
			botonSeleccionar.setVisibility(View.VISIBLE);		
		}
	}
	
	/* SECTION M�todos Listener onClick */
	
	private class OnAddClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			mCallback.onMainCreateButtonClicked();
		}
	}
	
	private class OnViewClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			mCallback.onMainSelectButtonClicked();
		}
	}
	
	private class OnGameClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			mCallback.onMainPlayButtonClicked();
		}
	}
	
	private class OnTestClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			mCallback.onMainTestButtonClicked();
		}
	}
}
