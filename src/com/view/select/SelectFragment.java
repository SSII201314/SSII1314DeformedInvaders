package com.view.select;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.alert.TextInputAlert;
import com.android.social.SocialConnector;
import com.android.storage.ExternalStorageManager;
import com.android.view.SwipeableViewPager;
import com.create.design.TPadre;
import com.project.data.Personaje;
import com.project.main.OpenGLFragment;
import com.project.main.R;
import com.view.display.DisplayGLSurfaceView;

public class SelectFragment extends OpenGLFragment
{
	private ExternalStorageManager manager;
	private SocialConnector connector;
	private SwipeableViewPager pager;
	
	private Personaje personaje;

	private DisplayGLSurfaceView canvas;
	private ImageButton botonCamara, botonRun, botonJump, botonCrouch, botonAttack;

	/* SECTION Constructora */
	
	public static final SelectFragment newInstance(Personaje p, SwipeableViewPager s, ExternalStorageManager m, SocialConnector c)
	{
		SelectFragment fragment = new SelectFragment();
		fragment.setParameters(p, s, m, c);
		return fragment;
	}
	
	private void setParameters(Personaje p, SwipeableViewPager s, ExternalStorageManager m, SocialConnector c)
	{	
		personaje = p;
		pager = s;
		manager = m;
		connector = c;
	}
	
	/* SECTION M�todos Fragment */
		
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View rootView = inflater.inflate(R.layout.fragment_select_layout, container, false);
 		
		// Instanciar Elementos de la GUI
		canvas = (DisplayGLSurfaceView) rootView.findViewById(R.id.displayGLSurfaceViewSelect1);
		canvas.setParameters(personaje, manager, TPadre.Select);
		
		botonCamara = (ImageButton) rootView.findViewById(R.id.imageButtonSelect1);
		botonRun = (ImageButton) rootView.findViewById(R.id.imageButtonSelect2);
		botonJump = (ImageButton) rootView.findViewById(R.id.imageButtonSelect3);
		botonCrouch = (ImageButton) rootView.findViewById(R.id.imageButtonSelect4);
		botonAttack = (ImageButton) rootView.findViewById(R.id.imageButtonSelect5);
		
		botonCamara.setOnClickListener(new OnCamaraClickListener());
		botonRun.setOnClickListener(new OnRunClickListener());
		botonJump.setOnClickListener(new OnJumpClickListener());
		botonCrouch.setOnClickListener(new OnCrouchClickListener());
		botonAttack.setOnClickListener(new OnAttackClickListener());
		
		setCanvasListener(canvas);
		
		reiniciarInterfaz();
		actualizarInterfaz();
        return rootView;
    }
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		
		botonCamara = null;
		canvas = null;
		pager = null;
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
		botonCamara.setVisibility(View.INVISIBLE);
		botonRun.setVisibility(View.INVISIBLE);
		botonJump.setVisibility(View.INVISIBLE);
		botonCrouch.setVisibility(View.INVISIBLE);
		botonAttack.setVisibility(View.INVISIBLE);
		
		botonCamara.setBackgroundResource(R.drawable.icon_share_picture);
	}
	
	@Override
	protected void actualizarInterfaz()
	{
		if(canvas.isEstadoReposo() || !canvas.isEstadoAnimacion())
		{
			botonCamara.setVisibility(View.VISIBLE);
		}
		
		if(canvas.isEstadoReposo() || canvas.isEstadoAnimacion())
		{
			botonRun.setVisibility(View.VISIBLE);
			botonJump.setVisibility(View.VISIBLE);
			botonCrouch.setVisibility(View.VISIBLE);
			botonAttack.setVisibility(View.VISIBLE);
		}
		
		if(canvas.isEstadoRetoque())
		{
			botonCamara.setBackgroundResource(R.drawable.icon_share_camara);
		}
		else if(canvas.isEstadoTerminado())
		{
			botonCamara.setBackgroundResource(R.drawable.icon_share_post);
		}
	}
	
	/* SECTION M�todos Listener onClick */
	
	private class OnCamaraClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if(canvas.isEstadoReposo())
			{
				canvas.seleccionarRetoque();
				pager.setSwipeable(false);
			}	
			else if(canvas.isEstadoRetoque())
			{
				Bitmap bitmap = canvas.getCapturaPantalla();
				if(manager.guardarImagen(bitmap, personaje.getNombre()))
				{
					String text = getString(R.string.text_social_photo_initial)+" "+personaje.getNombre()+" "+getString(R.string.text_social_photo_final);
					
					TextInputAlert alert = new TextInputAlert(getActivity(), getString(R.string.text_social_share_title), getString(R.string.text_social_share_description), text, getString(R.string.text_button_send), getString(R.string.text_button_cancel)) {
			
						@Override
						public void onPossitiveButtonClick()
						{
							connector.publicar(getText(), manager.cargarImagen(personaje.getNombre()));
							canvas.seleccionarTerminado();
							pager.setSwipeable(true);
							
							reiniciarInterfaz();
							actualizarInterfaz();
						}
		
						@Override
						public void onNegativeButtonClick()
						{
							canvas.seleccionarTerminado();
							pager.setSwipeable(true);
							
							reiniciarInterfaz();
							actualizarInterfaz();
						}
						
					};
		
					reiniciarInterfaz();
					actualizarInterfaz();
					alert.show();
				}
				else
				{
					Toast.makeText(getActivity(), R.string.error_picture_character, Toast.LENGTH_SHORT).show();
				}
				
				pager.setSwipeable(false);
			}	
			
			reiniciarInterfaz();
			actualizarInterfaz();
		}
	}
	
	private class OnRunClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if(canvas.getAnimacionFinalizada())
			{
				canvas.seleccionarRun();
				canvas.setAnimacionFinalizada(false);
			}
		}
	}
	
	private class OnJumpClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if(canvas.getAnimacionFinalizada())
			{
				canvas.seleccionarJump();
				canvas.setAnimacionFinalizada(false);
			}
		}
	}
	
	private class OnCrouchClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if(canvas.getAnimacionFinalizada())
			{
				canvas.seleccionarCrouch();
				canvas.setAnimacionFinalizada(false);
			}
			
		}
	}
	
	private class OnAttackClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if(canvas.getAnimacionFinalizada())
			{
				canvas.seleccionarAttack();
				canvas.setAnimacionFinalizada(false);
			}
			
		}
	}
}
