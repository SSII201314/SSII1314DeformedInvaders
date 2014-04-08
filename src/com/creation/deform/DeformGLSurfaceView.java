package com.creation.deform;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import com.android.touch.TTouchEstado;
import com.android.view.OpenGLSurfaceView;
import com.creation.data.Esqueleto;
import com.creation.data.Textura;
import com.lib.utils.FloatArray;
import com.project.main.GamePreferences;

public class DeformGLSurfaceView extends OpenGLSurfaceView
{
	private DeformOpenGLRenderer renderer;

	private Handler handler;
	private Runnable task;

	private boolean threadActivo;

	/* Constructora */

	public DeformGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs, TTouchEstado.MultiTouch);
	}

	public void setParameters(Esqueleto esqueleto, Textura textura, TDeformTipo tipo, final DeformFragment fragmento, int num_frames)
	{
		renderer = new DeformOpenGLRenderer(getContext(), esqueleto, textura, tipo, num_frames);
		setRenderer(renderer);

		handler = new Handler();

		task = new Runnable() {
			@Override
			public void run()
			{
				if (!renderer.reproducirAnimacion())
				{
					requestRender();
					handler.postDelayed(this, GamePreferences.TIME_INTERVAL_ANIMATION);
				}
				else
				{
					renderer.seleccionarReposo();
					fragmento.reiniciarInterfaz();
					fragmento.actualizarInterfaz();

					threadActivo = false;
				}
			}
		};

		threadActivo = false;
	}

	/* M�todos Abstr�ctos OpenGLSurfaceView */

	@Override
	protected boolean onTouchDown(float x, float y, float width, float height, int pos)
	{
		return renderer.onTouchDown(x, y, width, height, pos);
	}

	@Override
	protected boolean onTouchMove(float x, float y, float width, float height, int pos)
	{
		return renderer.onTouchMove(x, y, width, height, pos);
	}

	@Override
	protected boolean onTouchUp(float x, float y, float width, float height, int pos)
	{
		return renderer.onTouchUp(x, y, width, height, pos);
	}

	@Override
	protected boolean onMultiTouchEvent()
	{
		return renderer.onMultiTouchEvent();
	}

	/* M�todos de modifiaci�n del Renderer */

	public void seleccionarAnyadir()
	{
		renderer.seleccionarAnyadir();
	}

	public void seleccionarEliminar()
	{
		renderer.seleccionarEliminar();
	}

	public void seleccionarMover()
	{
		renderer.seleccionarMover();
	}

	public void reiniciar()
	{
		renderer.reiniciar();
		requestRender();
	}

	public void seleccionarGrabado()
	{
		renderer.seleccionarGrabado();
		requestRender();
	}

	public void seleccionarPlay()
	{
		if (!threadActivo)
		{
			renderer.selecionarPlay();
			requestRender();

			task.run();
			threadActivo = true;
		}
	}

	public void seleccionarAudio()
	{
		renderer.seleccionarAudio();
	}

	public void seleccionarReposo()
	{
		renderer.seleccionarReposo();
	}

	/* M�todos de Obtenci�n de Informaci�n */

	public boolean isHandlesVacio()
	{
		return renderer.isHandlesVacio();
	}

	public boolean isEstadoAnyadir()
	{
		return renderer.isEstadoAnyadir();
	}

	public boolean isEstadoEliminar()
	{
		return renderer.isEstadoEliminar();
	}

	public boolean isEstadoDeformar()
	{
		return renderer.isEstadoDeformar();
	}

	public boolean isEstadoGrabacion()
	{
		return renderer.isEstadoGrabacion();
	}

	public boolean isGrabacionReady()
	{
		return renderer.isGrabacionReady();
	}

	public boolean isEstadoAudio()
	{
		return renderer.isEstadoAudio();
	}

	public boolean isEstadoReproduccion()
	{
		return renderer.isEstadoReproduccion();
	}

	public List<FloatArray> getMovimientos()
	{
		return renderer.getMovimientos();
	}

	/* M�todos de Guardado de Informaci�n */

	public DeformDataSaved saveData()
	{
		return renderer.saveData();
	}

	public void restoreData(DeformDataSaved data)
	{
		renderer.restoreData(data);
	}

}
