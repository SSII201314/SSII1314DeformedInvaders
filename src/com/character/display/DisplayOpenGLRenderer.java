package com.character.display;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.view.OpenGLRenderer;
import com.creation.data.MapaBits;
import com.game.data.Personaje;

public class DisplayOpenGLRenderer extends OpenGLRenderer 
{
	private TEstadoDisplay estado;

	// Personaje
	private Personaje personaje;
	private boolean personajeCargado;

	// Captura
	private Bitmap captura;
	private TEstadoCaptura estadoCaptura;

	/* Constructura */

	public DisplayOpenGLRenderer(Context context)
	{
		super(context);

		personajeCargado = false;

		estado = TEstadoDisplay.Nada;
		estadoCaptura = TEstadoCaptura.Nada;
	}

	public DisplayOpenGLRenderer(Context context, Personaje p)
	{
		super(context);

		personajeCargado = true;
		personaje = p;

		estado = TEstadoDisplay.Nada;
		estadoCaptura = TEstadoCaptura.Nada;
	}

	/* M�todos Renderer */

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		super.onSurfaceCreated(gl, config);

		if (personajeCargado)
		{
			personaje.cargarTextura(gl, this, mContext);
		}
	}

	@Override
	public void onDrawFrame(GL10 gl)
	{
		super.onDrawFrame(gl);

		if (personajeCargado)
		{
			// Centrado de Marco
			centrarPersonajeEnMarcoInicio(gl);

			personaje.dibujar(gl, this);

			// Centrado de Marco
			centrarPersonajeEnMarcoFinal(gl);

			if (estado == TEstadoDisplay.Nada || estado == TEstadoDisplay.Captura)
			{
				if (estado == TEstadoDisplay.Captura)
				{
					if (estadoCaptura == TEstadoCaptura.Capturando)
					{
						// Capturar Pantalla
						MapaBits textura = capturaPantalla(gl);
						captura = textura.getBitmap();

						// Desactivar Modo Captura
						estadoCaptura = TEstadoCaptura.Terminado;

						// Restaurar posici�n anterior de la C�mara
						camaraRestore();

						// Reiniciar Renderer
						super.onDrawFrame(gl);

						// Centrado de Marco
						centrarPersonajeEnMarcoInicio(gl);

						personaje.dibujar(gl, this);

						// Centrado de Marco
						centrarPersonajeEnMarcoFinal(gl);
					}
					else if (estadoCaptura == TEstadoCaptura.Retocando)
					{
						// Marco Oscuro
						dibujarMarcoExterior(gl);
					}
				}
			}
		}
	}

	/* M�todos abstractos de OpenGLRenderer */

	@Override
	protected boolean reiniciar()
	{
		return false;
	}

	@Override
	protected boolean onTouchDown(float pixelX, float pixelY, float screenWidth, float screenHeight, int pointer)
	{
		return false;
	}

	@Override
	protected boolean onTouchMove(float pixelX, float pixelY, float screenWidth, float screenHeight, int pointer)
	{
		return false;
	}

	@Override
	protected boolean onTouchUp(float pixelX, float pixelY, float screenWidth, float screenHeight, int pointer)
	{
		return false;
	}

	@Override
	protected boolean onMultiTouchEvent()
	{
		return false;
	}

	/* M�todos de Modificaci�n de Estado */

	public void seleccionarRetoque(float height, float width)
	{
		// Construir rectangulos
		estado = TEstadoDisplay.Captura;
		estadoCaptura = TEstadoCaptura.Retocando;
	}

	public void seleccionarCaptura()
	{
		if (estado == TEstadoDisplay.Captura)
		{
			estadoCaptura = TEstadoCaptura.Capturando;
		}
	}

	public void seleccionarTerminado()
	{
		if (estado == TEstadoDisplay.Captura)
		{
			estado = TEstadoDisplay.Nada;
			estadoCaptura = TEstadoCaptura.Nada;
		}
	}

	public boolean reproducirAnimacion()
	{
		return personaje.animar(false);
	}

	public void seleccionarReposo()
	{
		personaje.reposo();
	}

	public void seleccionarRun()
	{
		estado = TEstadoDisplay.Run;
		personaje.mover();
	}

	public void seleccionarJump()
	{
		estado = TEstadoDisplay.Jump;
		personaje.saltar();
	}

	public void seleccionarCrouch()
	{
		estado = TEstadoDisplay.Crouch;
		personaje.agachar();
	}

	public void seleccionarAttack()
	{
		estado = TEstadoDisplay.Attack;
		personaje.atacar();
	}

	/* M�todos de Obtenci�n de Informaci�n */

	public boolean isEstadoReposo()
	{
		return estado == TEstadoDisplay.Nada;
	}

	public boolean isEstadoRetoque()
	{
		return estado == TEstadoDisplay.Captura && estadoCaptura == TEstadoCaptura.Retocando;
	}

	public boolean isEstadoCapturando()
	{
		return estado == TEstadoDisplay.Captura && estadoCaptura == TEstadoCaptura.Retocando;
	}

	public boolean isEstadoTerminado()
	{
		return estado == TEstadoDisplay.Captura && estadoCaptura == TEstadoCaptura.Terminado;
	}

	public boolean isEstadoAnimacion()
	{
		return estado != TEstadoDisplay.Nada && estado != TEstadoDisplay.Captura;
	}

	public Bitmap getCapturaPantalla()
	{
		if (estadoCaptura == TEstadoCaptura.Capturando)
		{
			while (estadoCaptura != TEstadoCaptura.Terminado);

			return captura;
		}

		return null;
	}

	/* M�todos de Guardado de Informaci�n */

	public void saveData()
	{
		if (personajeCargado)
		{
			personaje.descargarTextura(this);
		}
	}
}