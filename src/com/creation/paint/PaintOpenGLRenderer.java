package com.creation.paint;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;

import com.android.view.OpenGLRenderer;
import com.character.display.TEstadoCaptura;
import com.creation.data.Accion;
import com.creation.data.Esqueleto;
import com.creation.data.Handle;
import com.creation.data.MapaBits;
import com.creation.data.Pegatinas;
import com.creation.data.Polilinea;
import com.creation.data.Textura;
import com.game.data.TTipoSticker;
import com.lib.math.GeometryUtils;
import com.lib.math.Intersector;
import com.lib.opengl.BufferManager;
import com.lib.utils.FloatArray;
import com.lib.utils.ShortArray;
import com.project.main.GamePreferences;
import com.project.main.R;

public class PaintOpenGLRenderer extends OpenGLRenderer
{
	// Estructura de Datos
	private TEstadoPaint estado;

	private int colorPaleta;
	private int sizeLinea;
	private int pegatinaActual;
	private TTipoSticker tipoPegatinaActual;

	// Detalles
	private List<Polilinea> listaLineas;
	private FloatArray lineaActual;
	private FloatBuffer bufferLineaActual;

	// Pegatinas
	private Pegatinas pegatinas;
	private boolean pegatinaAnyadida;

	// Esqueleto
	private ShortArray contorno;
	private FloatBuffer bufferContorno;

	private FloatArray vertices;
	private FloatBuffer bufferVertices;

	private ShortArray triangulos;

	private int color;

	// Texturas
	private TEstadoCaptura estadoCaptura;

	private MapaBits textura;
	private FloatArray coordsTextura;

	private Handle objetoVertice;

	// Anterior Siguiente Buffers
	private Stack<Accion> anteriores;
	private Stack<Accion> siguientes;

	/* Constructora */

	public PaintOpenGLRenderer(Context context, Esqueleto esqueleto)
	{
		super(context);

		estado = TEstadoPaint.Nada;

		contorno = esqueleto.getContorno();
		vertices = esqueleto.getVertices();
		triangulos = esqueleto.getTriangulos();

		bufferVertices = BufferManager.construirBufferListaTriangulosRellenos(triangulos, vertices);
		bufferContorno = BufferManager.construirBufferListaIndicePuntos(contorno, vertices);

		listaLineas = new ArrayList<Polilinea>();
		lineaActual = null;

		pegatinas = new Pegatinas();
		pegatinaActual = 0;
		pegatinaAnyadida = false;

		color = Color.WHITE;

		colorPaleta = Color.RED;
		sizeLinea = 6;

		anteriores = new Stack<Accion>();
		siguientes = new Stack<Accion>();

		estadoCaptura = TEstadoCaptura.Nada;

		objetoVertice = new Handle(20, POINTWIDTH);
	}

	/* M�todos Renderer */

	@Override
	public void onDrawFrame(GL10 gl)
	{
		if (estado == TEstadoPaint.Captura && estadoCaptura == TEstadoCaptura.Capturando)
		{
			// Guardar posici�n actual de la C�mara
			salvarCamara();

			// Restaurar C�mara posici�n inicial
			camaraRestore();

			dibujarEsqueleto(gl);

			// Capturar Pantalla
			textura = capturaPantalla(gl);

			// Construir Textura
			coordsTextura = construirTextura(vertices, textura.getWidth(), textura.getHeight());

			// Desactivar Modo Captura
			estadoCaptura = TEstadoCaptura.Terminado;

			// Restaurar posici�n anterior de la C�mara
			recuperarCamara();
		}

		// Cargar Pegatinas
		for (int i = 0; i < GamePreferences.MAX_TEXTURE_STICKER; i++)
		{
			TTipoSticker tipoPegatinas = TTipoSticker.values()[i];
			
			if (pegatinas.isCargada(tipoPegatinas))
			{
				cargarTexturaRectangulo(gl, pegatinas.getIndice(tipoPegatinas, mContext), tipoPegatinas);
			}
		}

		dibujarEsqueleto(gl);
	}

	private void dibujarEsqueleto(GL10 gl)
	{
		super.onDrawFrame(gl);

		// Centrado de Marco
		centrarPersonajeEnMarcoInicio(gl);

		// Esqueleto
		dibujarBuffer(gl, GL10.GL_TRIANGLES, SIZELINE, color, bufferVertices);

		// Detalles
		if (lineaActual != null)
		{
			dibujarBuffer(gl, GL10.GL_LINE_STRIP, sizeLinea, colorPaleta, bufferLineaActual);
		}

		Iterator<Polilinea> it = listaLineas.iterator();
		while (it.hasNext())
		{
			Polilinea polilinea = it.next();
			dibujarBuffer(gl, GL10.GL_LINE_STRIP, polilinea.getSize(), polilinea.getColor(), polilinea.getBuffer());
		}

		if (estado != TEstadoPaint.Captura)
		{
			// Contorno
			dibujarBuffer(gl, GL10.GL_LINE_LOOP, SIZELINE, Color.BLACK, bufferContorno);

			// Dibujar Pegatinas
			for (int i = 0; i < GamePreferences.MAX_TEXTURE_STICKER; i++)
			{
				TTipoSticker tipoPegatinas = TTipoSticker.values()[i];
				
				if (pegatinas.isCargada(tipoPegatinas))
				{
					int indice = pegatinas.getVertice(tipoPegatinas);
					dibujarTexturaRectangulo(gl, vertices.get(2 * indice), vertices.get(2 * indice + 1), tipoPegatinas);
				}
			}

			if (estado == TEstadoPaint.Pegatinas)
			{
				dibujarListaHandle(gl, Color.BLACK, objetoVertice.getBuffer(), vertices);
			}
		}

		// Centrado de Marco
		centrarPersonajeEnMarcoFinal(gl);
	}

	/* M�todos Abstr�ctos OpenGLRenderer */

	@Override
	protected boolean reiniciar()
	{
		lineaActual = null;
		listaLineas.clear();

		pegatinas = new Pegatinas();
		pegatinaActual = 0;
		pegatinaAnyadida = false;

		for (int i = 0; i < GamePreferences.MAX_TEXTURE_STICKER; i++)
		{
			TTipoSticker[] tipoPegatinas = TTipoSticker.values();
			descargarTexturaRectangulo(tipoPegatinas[i]);
		}

		anteriores.clear();
		siguientes.clear();

		estado = TEstadoPaint.Nada;
		color = Color.WHITE;
		sizeLinea = 6;

		return true;
	}

	@Override
	protected boolean onTouchDown(float pixelX, float pixelY, float screenWidth, float screenHeight, int pointer)
	{
		if (estado == TEstadoPaint.Pincel)
		{
			return anyadirPunto(pixelX, pixelY, screenWidth, screenHeight);
		}
		else if (estado == TEstadoPaint.Cubo)
		{
			return pintarEsqueleto(pixelX, pixelY, screenWidth, screenHeight);
		}
		else if (estado == TEstadoPaint.Pegatinas)
		{
			return anyadirPegatina(pixelX, pixelY, screenWidth, screenHeight);
		}

		return false;
	}

	private boolean anyadirPunto(float pixelX, float pixelY, float screenWidth, float screenHeight)
	{
		// Conversi�n Pixel - Punto
		float worldX = convertToWorldXCoordinate(pixelX, screenWidth);
		float worldY = convertToWorldYCoordinate(pixelY, screenHeight);

		if (lineaActual == null)
		{
			lineaActual = new FloatArray();
		}

		boolean anyadir = true;

		if (lineaActual.size > 0)
		{
			float lastWorldX = lineaActual.get(lineaActual.size - 2);
			float lastWorldY = lineaActual.get(lineaActual.size - 1);

			float lastPixelX = convertToPixelXCoordinate(lastWorldX, screenWidth);
			float lastPixelY = convertToPixelYCoordinate(lastWorldY, screenHeight);

			anyadir = Math.abs(Intersector.distancePoints(pixelX, pixelY, lastPixelX, lastPixelY)) > GamePreferences.MAX_DISTANCE_PIXELS;
		}

		if (anyadir)
		{
			float frameX = convertToFrameXCoordinate(worldX);
			float frameY = convertToFrameYCoordinate(worldY);

			lineaActual.add(frameX);
			lineaActual.add(frameY);

			bufferLineaActual = BufferManager.construirBufferListaPuntos(lineaActual);

			return true;
		}

		return false;
	}

	private boolean pintarEsqueleto(float pixelX, float pixelY, float screenWidth, float screenHeight)
	{
		// Conversi�n Pixel - Punto
		float worldX = convertToWorldXCoordinate(pixelX, screenWidth);
		float worldY = convertToWorldYCoordinate(pixelY, screenHeight);

		float frameX = convertToFrameXCoordinate(worldX);
		float frameY = convertToFrameYCoordinate(worldY);

		if (GeometryUtils.isPointInsideMesh(contorno, vertices, frameX, frameY))
		{
			if (colorPaleta != color)
			{
				color = colorPaleta;

				anteriores.push(new Accion(colorPaleta));
				siguientes.clear();

				return true;
			}
		}

		return false;
	}

	private boolean anyadirPegatina(float pixelX, float pixelY, float screenWidth, float screenHeight)
	{
		// Pixel pertenece a los V�rtices
		short j = buscarPixel(vertices, pixelX, pixelY, screenWidth, screenHeight);
		if (j != -1)
		{
			pegatinas.setPegatina(pegatinaActual, j, tipoPegatinaActual);

			descargarTexturaRectangulo(tipoPegatinaActual);
			pegatinaAnyadida = true;

			anteriores.push(new Accion(pegatinaActual, j, tipoPegatinaActual));
			siguientes.clear();

			return true;
		}

		return false;
	}

	@Override
	protected boolean onTouchMove(float pixelX, float pixelY, float screenWidth, float screenHeight, int pointer)
	{
		if (estado == TEstadoPaint.Pincel)
		{
			return onTouchDown(pixelX, pixelY, screenWidth, screenHeight, pointer);
		}

		return false;
	}

	@Override
	protected boolean onTouchUp(float pixelX, float pixelY, float screenWidth, float screenHeight, int pointer)
	{
		if (estado == TEstadoPaint.Pincel)
		{
			return guardarPolilinea();
		}

		return false;
	}

	@Override
	protected boolean onMultiTouchEvent()
	{
		return false;
	}

	private boolean guardarPolilinea()
	{
		if (lineaActual != null)
		{
			Polilinea polilinea = new Polilinea(colorPaleta, sizeLinea, lineaActual, bufferLineaActual);

			listaLineas.add(polilinea);
			anteriores.push(new Accion(polilinea));
			siguientes.clear();
			lineaActual = null;

			return true;
		}

		return false;
	}

	/* M�todos de Selecci�n de Estado */

	public void seleccionarMano()
	{
		guardarPolilinea();
		estado = TEstadoPaint.Mano;
	}

	public void seleccionarPincel()
	{
		guardarPolilinea();
		estado = TEstadoPaint.Pincel;
	}

	public void seleccionarCubo()
	{
		guardarPolilinea();
		estado = TEstadoPaint.Cubo;
	}

	public void seleccionarColor(int color)
	{
		colorPaleta = color;
	}

	public int getColorPaleta()
	{
		return colorPaleta;
	}

	public void setColorPaleta(int colorPaleta)
	{
		this.colorPaleta = colorPaleta;
	}

	public void seleccionarSize(int pos)
	{
		if (pos == 0)
		{
			sizeLinea = 6;
		}
		else if (pos == 1)
		{
			sizeLinea = 11;
		}
		else if (pos == 2)
		{
			sizeLinea = 16;
		}
	}

	public void seleccionarPegatina(int pegatina, TTipoSticker tipo)
	{
		guardarPolilinea();

		pegatinaActual = pegatina;
		tipoPegatinaActual = tipo;
		estado = TEstadoPaint.Pegatinas;
	}

	public void seleccionarCaptura() 
{
		guardarPolilinea();

		estado = TEstadoPaint.Captura;
		estadoCaptura = TEstadoCaptura.Capturando;
	}

	/* M�todos de modificaci�n de Buffers de estado */

	public void anteriorAccion()
	{
		guardarPolilinea();

		if (!anteriores.isEmpty())
		{
			Accion accion = anteriores.pop();
			siguientes.add(accion);
			actualizarEstado(anteriores);
		}
	}

	public void siguienteAccion()
	{
		guardarPolilinea();

		if (!siguientes.isEmpty())
		{
			Accion accion = siguientes.lastElement();
			siguientes.remove(siguientes.size() - 1);
			anteriores.push(accion);
			actualizarEstado(anteriores);
		}
	}

	private void actualizarEstado(Stack<Accion> pila)
	{
		color = Color.WHITE;
		listaLineas = new ArrayList<Polilinea>();
		pegatinas = new Pegatinas();

		for (int i = 0; i < GamePreferences.MAX_TEXTURE_STICKER; i++)
		{
			TTipoSticker[] tipoPegatinas = TTipoSticker.values();
			descargarTexturaRectangulo(tipoPegatinas[i]);
		}

		Iterator<Accion> it = pila.iterator();
		while (it.hasNext())
		{
			Accion accion = it.next();
			if (accion.isTipoColor())
			{
				color = accion.getColor();
			}
			else if (accion.isTipoPolilinea())
			{
				listaLineas.add(accion.getLinea());
			}
			else if (accion.isTipoPegatina())
			{
				pegatinas.setPegatina(accion.getIndicePegatina(), accion.getVerticePegatina(), accion.getTipoPegatina());
			}
		}
	}

	/* M�todos de Obtenci�n de Informaci�n */

	public boolean isBufferSiguienteVacio()
	{
		return siguientes.isEmpty();
	}

	public boolean isBufferAnteriorVacio()
	{
		return anteriores.isEmpty();
	}

	public boolean isPegatinaAnyadida()
	{
		if (pegatinaAnyadida)
		{
			pegatinaAnyadida = false;
			estado = TEstadoPaint.Nada;

			return true;
		}

		return false;
	}

	public boolean isEstadoPincel()
	{
		return estado == TEstadoPaint.Pincel;
	}

	public boolean isEstadoCubo()
	{
		return estado == TEstadoPaint.Cubo;
	}

	public boolean isEstadoMover()
	{
		return estado == TEstadoPaint.Mano;
	}

	public boolean isEstadoPegatinas()
	{
		return estado == TEstadoPaint.Pegatinas;
	}

	public Textura getTextura()
	{
		if (estadoCaptura == TEstadoCaptura.Capturando)
		{
			final ProgressDialog alert = ProgressDialog.show(mContext, mContext.getString(R.string.text_processing_character_title), mContext.getString(R.string.text_processing_character_description), true);

			Thread thread = new Thread(new Runnable() {
				@Override
				public void run()
				{
					while (estadoCaptura != TEstadoCaptura.Terminado);

					estado = TEstadoPaint.Nada;
					estadoCaptura = TEstadoCaptura.Nada;
					
					alert.dismiss();
				}
			});
			
			thread.start();

			// Esperar por la finalizaci�n del thread.
			
			try
			{
				thread.join();
				
				return new Textura(textura, coordsTextura, pegatinas);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	/* M�todos de Guardado de Informaci�n */

	public PaintDataSaved saveData()
	{
		return new PaintDataSaved(anteriores, siguientes, estado);
	}

	public void restoreData(PaintDataSaved data)
	{
		estado = data.getEstado();
		anteriores = data.getAnteriores();
		siguientes = data.getSiguientes();

		actualizarEstado(anteriores);
	}
}
