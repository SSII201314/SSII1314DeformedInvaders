package com.project.main;

import java.io.File;
import java.util.List;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.alert.ConfirmationAlert;
import com.android.alert.ImageAlert;
import com.android.alert.SummaryAlert;
import com.android.alert.TextInputAlert;
import com.android.audio.AudioPlayerManager;
import com.android.social.SocialConnector;
import com.android.storage.ExternalStorageManager;
import com.android.storage.InternalStorageManager;
import com.character.select.CharacterSelectionFragment;
import com.creation.data.Esqueleto;
import com.creation.data.Movimientos;
import com.creation.data.Textura;
import com.creation.deform.AnimationFragment;
import com.creation.design.DesignFragment;
import com.creation.paint.PaintFragment;
import com.game.data.Personaje;
import com.game.game.GameFragment;
import com.game.select.LevelGenerator;
import com.game.select.LevelSelectionFragment;

public class MainActivity extends FragmentActivity implements LoadingFragment.LoadingFragmentListener, MainFragment.MainFragmentListener, DesignFragment.DesignFragmentListener, PaintFragment.PaintFragmentListener, AnimationFragment.AnimationFragmentListener, CharacterSelectionFragment.CharacterSelectionFragmentListener, LevelSelectionFragment.LevelSelectionFragmentListener, GameFragment.GameFragmentListener
{
	/* Estructura de Datos */
	private List<Personaje> listaPersonajes;
	private Personaje personajeActual;
	private int personajeSeleccionado;
	
	/* Musica */
	private AudioPlayerManager audioManager;

	/* Almacenamiento */
	private InternalStorageManager internalManager;
	private ExternalStorageManager externalManager;

	/* Conectores Sociales */
	private SocialConnector socialConnector;

	/* Elementos de la Interafaz */
	private ActionBar actionBar;
	private MenuItem botonTwitter, botonFacebook, botonMusica;

	/* Estado */
	private TEstado estado;

	/* Niveles */
	private LevelGenerator levelGenerator;
	private boolean[] estadoNiveles;
	private int[] puntuacionNiveles;

	/* M�todos Activity */

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_layout);

		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		internalManager = new InternalStorageManager(this);
		externalManager = new ExternalStorageManager();
		
		levelGenerator = new LevelGenerator(this);
		
		socialConnector = new SocialConnector(this) {
			@Override
			public void onConectionStatusChange()
			{
				actualizarActionBar();
			}
		};
		
		audioManager = new AudioPlayerManager(this) {
			@Override
			public void onPlayerCompletion() { }
		};

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		changeFragment(LoadingFragment.newInstance(internalManager));
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		audioManager.resumePlaying();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		audioManager.pausePlaying();
	}

	@Override
	public void onBackPressed()
	{
		if (estado != TEstado.Main && estado != TEstado.Game)
		{
			limpiarActionBar();

			super.onBackPressed();
			actualizarEstado();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		
		botonMusica = menu.getItem(0);
		botonTwitter = menu.getItem(1);
		botonFacebook = menu.getItem(2);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menuIcon1:
				return onMenuMusicButtonClicked();
			case R.id.menuIcon2:
				return onMenuTwitterButtonClicked();
			case R.id.menuIcon3:
				return onMenuFacebookButtonClicked();
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/* M�todos de Modificaci�n del FrameLayout */

	private void changeFragment(Fragment fragmento)
	{
		boolean clearBackStack = false;
		boolean addToBackStack = true;

		if (estado == TEstado.CharacterSelection || estado == TEstado.LevelSelection || estado == TEstado.Game)
		{
			addToBackStack = false;
		}

		actualizarEstado(fragmento);
		limpiarActionBar();

		if (estado == TEstado.Main)
		{
			clearBackStack = true;
		}

		FragmentManager manager = getSupportFragmentManager();

		// Limpiar la BackStack
		if (clearBackStack)
		{
			clearBackStack(manager);
		}

		// Reemplazar el Fragmento
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(R.id.frameLayoutMain1, fragmento, estado.toString());

		// A�adir a la BackStack
		if (addToBackStack)
		{
			transaction.addToBackStack(estado.toString());
		}

		transaction.commit();
		
		actualizarMusica();
	}

	private void clearBackStack(FragmentManager manager)
	{
		manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	/* M�todos Loading Fragment */

	@Override
	public void onLoadingListCharacters(List<Personaje> lista, int seleccionado, boolean[] niveles, int[] puntuacion)
	{
		listaPersonajes = lista;
		personajeSeleccionado = seleccionado;
		estadoNiveles = niveles;
		puntuacionNiveles = puntuacion;
		
		changeFragment(MainFragment.newInstance(listaPersonajes, personajeSeleccionado, externalManager));
	}

	/* M�todos Main Fragment */

	@Override
	public void onMainCreateButtonClicked()
	{
		personajeActual = new Personaje();

		changeFragment(DesignFragment.newInstance());
	}

	@Override
	public void onMainSelectButtonClicked()
	{
		changeFragment(CharacterSelectionFragment.newInstance(listaPersonajes, externalManager, socialConnector));
	}

	@Override
	public void onMainPlayButtonClicked()
	{
		changeFragment(LevelSelectionFragment.newInstance(levelGenerator.getListaNiveles(), estadoNiveles));
	}

	/* M�todos Design Fragment */

	@Override
	public void onDesignReadyButtonClicked(Esqueleto esqueleto)
	{
		if (esqueleto == null)
		{
			Toast.makeText(getApplication(), R.string.error_design, Toast.LENGTH_SHORT).show();
		}
		else
		{
			personajeActual.setEsqueleto(esqueleto);
			changeFragment(PaintFragment.newInstance(personajeActual.getEsqueleto()));
		}
	}

	/* M�todos Paint Fragment */

	@Override
	public void onPaintReadyButtonClicked(Textura textura)
	{
		if (textura == null)
		{
			Toast.makeText(getApplication(), R.string.error_paint, Toast.LENGTH_SHORT).show();
		}
		else
		{
			personajeActual.setTextura(textura);
			changeFragment(AnimationFragment.newInstance(personajeActual.getEsqueleto(), personajeActual.getTextura(), externalManager));
		}
	}

	/* M�todos Animation Fragment */

	@Override
	public void onAnimationReadyButtonClicked(Movimientos movimientos)
	{
		if (movimientos == null)
		{
			Toast.makeText(getApplication(), R.string.error_animation, Toast.LENGTH_SHORT).show();
		}
		else
		{
			if (movimientos.isReady())
			{
				personajeActual.setMovimientos(movimientos);

				TextInputAlert alert = new TextInputAlert(this, getString(R.string.text_save_character_title), getString(R.string.text_save_character_description), getString(R.string.text_button_yes), getString(R.string.text_button_no)) {
					@Override
					public void onPossitiveButtonClick(String text)
					{
						personajeActual.setNombre(text);

						if (internalManager.guardarPersonaje(personajeActual))
						{
							externalManager.guardarAudio(text, getString(R.string.title_animation_section_run));
							externalManager.guardarAudio(text, getString(R.string.title_animation_section_jump));
							externalManager.guardarAudio(text, getString(R.string.title_animation_section_crouch));
							externalManager.guardarAudio(text, getString(R.string.title_animation_section_attack));

							listaPersonajes.add(personajeActual);
							personajeActual = null;

							Toast.makeText(getApplication(), R.string.text_save_character_confirmation, Toast.LENGTH_SHORT).show();
						}
						else
						{
							Toast.makeText(getApplication(), R.string.error_save_character, Toast.LENGTH_SHORT).show();
						}

						changeFragment(MainFragment.newInstance(listaPersonajes, personajeSeleccionado, externalManager));
					}

					@Override
					public void onNegativeButtonClick(String text)
					{
						changeFragment(MainFragment.newInstance(listaPersonajes, personajeSeleccionado, externalManager));
					}

				};

				alert.show();
			}
			else
			{
				Toast.makeText(getApplication(), R.string.error_deform, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/* M�todos Character Selection Fragment */

	@Override
	public void onCharacterSelectionSelectClicked(int indice)
	{
		personajeSeleccionado = indice;

		internalManager.guardarSeleccionado(personajeSeleccionado);
		Toast.makeText(getApplication(), R.string.text_select_character_confirmation, Toast.LENGTH_SHORT).show();

		changeFragment(MainFragment.newInstance(listaPersonajes, personajeSeleccionado, externalManager));
	}

	@Override
	public void onCharacterSelectionDeleteButtonClicked(final int indice)
	{
		ConfirmationAlert alert = new ConfirmationAlert(this, getString(R.string.text_delete_character_title), getString(R.string.text_delete_character_description), getString(R.string.text_button_ok), getString(R.string.text_button_no)) {
			@Override
			public void onPossitiveButtonClick()
			{
				if (internalManager.eliminarPersonaje(listaPersonajes.get(indice)))
				{
					externalManager.eliminarDirectorioPersonaje(listaPersonajes.get(indice).getNombre());
					listaPersonajes.remove(indice);

					if (personajeSeleccionado == indice)
					{
						personajeSeleccionado = -1;
						internalManager.guardarSeleccionado(personajeSeleccionado);
					}

					Toast.makeText(getApplication(), R.string.text_delete_character_confirmation, Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(getApplication(), R.string.error_delete_character, Toast.LENGTH_SHORT).show();
				}

				changeFragment(MainFragment.newInstance(listaPersonajes, personajeSeleccionado, externalManager));
			}

			@Override
			public void onNegativeButtonClick() { }
		};

		alert.show();
	}

	/* M�todos Level Selection Fragment */

	@Override
	public void onLevelSelectionSelectClicked(int level)
	{	
		changeFragment(GameFragment.newInstance(listaPersonajes.get(personajeSeleccionado), externalManager, levelGenerator.getLevel(level)));
	
		// Resumen del nivel
		SummaryAlert alert = new SummaryAlert(this, getString(R.string.text_summary), getString(R.string.text_button_ready), levelGenerator.getLevel(level).getTipoEnemigos());
		alert.show();	
	}

	/* M�todos Game Fragment */

	@Override
	public void onGameFinished(final int score, final int level, final int idImagen, final String nameLevel)
	{
		// Sonido Victoria
		audioManager.startPlaying(R.raw.effect_level_complete, false);
		
		// Desbloquear Siguiente nivel
		int nextLevel = (level + 1) % estadoNiveles.length;
		estadoNiveles[nextLevel] = true;
		
		// Actualizar Puntuacion m�xima
		if (score > puntuacionNiveles[level])
		{
			puntuacionNiveles[level] = score;
		}
		
		internalManager.guardarNiveles(estadoNiveles);
		internalManager.guardarPuntuacion(puntuacionNiveles);

		// Publicar Nivel Completado
		String nombreFotoNivel = "nivel" + level;
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), idImagen);
		if (externalManager.guardarImagenTemp(bitmap, nombreFotoNivel))
		{
			String text = getString(R.string.text_social_level_completed_initial) + " " + nameLevel + " " + getString(R.string.text_social_level_completed_middle) + " " + score + " " + getString(R.string.text_social_level_completed_final);

			File foto = externalManager.cargarImagenTemp(nombreFotoNivel);
			socialConnector.publicar(text, foto);
		}

		// Seleccionar Siguiente Nivel
		ImageAlert alert = new ImageAlert(this, getString(R.string.text_game_finish) + " " + score, getString(R.string.text_button_replay), getString(R.string.text_button_levels), idImagen) {
			@Override
			public void onPossitiveButtonClick()
			{
				changeFragment(GameFragment.newInstance(listaPersonajes.get(personajeSeleccionado), externalManager, levelGenerator.getLevel(level)));
			}

			@Override
			public void onNegativeButtonClick()
			{
				changeFragment(LevelSelectionFragment.newInstance(levelGenerator.getListaNiveles(), estadoNiveles));
			}
		};

		alert.show();
	}

	@Override
	public void onGameFailed(final int level, final int idImagen)
	{
		// Sonido Derrota
		audioManager.startPlaying(R.raw.effect_game_over, false);
		
		ImageAlert alert = new ImageAlert(this, getString(R.string.text_game_fail), getString(R.string.text_button_replay), getString(R.string.text_button_levels), idImagen) {
			@Override
			public void onPossitiveButtonClick()
			{
				changeFragment(GameFragment.newInstance(listaPersonajes.get(personajeSeleccionado), externalManager, levelGenerator.getLevel(level)));
			}

			@Override
			public void onNegativeButtonClick()
			{
				changeFragment(LevelSelectionFragment.newInstance(levelGenerator.getListaNiveles(), estadoNiveles));
			}
		};

		alert.show();
	}

	/* M�todos de Modificaci�n de la ActionBar */

	public boolean onMenuTwitterButtonClicked()
	{
		if (socialConnector.isTwitterConnected())
		{
			socialConnector.desconectarTwitter();
		}
		else
		{
			socialConnector.conectarTwitter();
		}
		
		return true;
	}

	public boolean onMenuFacebookButtonClicked()
	{
		if (socialConnector.isFacebookConnected())
		{
			socialConnector.desconectarFacebook();
		}
		else
		{
			socialConnector.conectarFacebook();
		}
		
		return true;
	}
	
	public boolean onMenuMusicButtonClicked()
	{
		if (audioManager.isEnabled())
		{
			audioManager.disablePlayer();
		}
		else
		{
			audioManager.enablePlayer();
			actualizarMusica();
		}
		
		actualizarActionBar();
		
		return true;
	}

	public void actualizarActionBar()
	{
		if (socialConnector.isTwitterConnected())
		{
			botonTwitter.setIcon(R.drawable.icon_social_twitter_connected);
		}
		else
		{
			botonTwitter.setIcon(R.drawable.icon_social_twitter_disconnected);
		}

		if (socialConnector.isFacebookConnected())
		{
			botonFacebook.setIcon(R.drawable.icon_social_facebook_connected);
		}
		else
		{
			botonFacebook.setIcon(R.drawable.icon_social_facebook_disconnected);
		}
		
		if (audioManager.isEnabled())
		{
			botonMusica.setIcon(R.drawable.icon_media_music_selected);
		}
		else
		{
			botonMusica.setIcon(R.drawable.icon_media_music);
		}
	}

	private void limpiarActionBar()
	{
		actionBar.removeAllTabs();
	}

	/* M�todos de Modificaci�n del Estado */

	private void actualizarMusica()
	{
		if(estado == TEstado.Game)
		{
			audioManager.startPlaying(R.raw.music_game, true);
		}
		else
		{
			if(audioManager.isStoped())
			{
				audioManager.startPlaying(R.raw.music_main, true);
			}
		}
	}
	
	private void actualizarEstado(Fragment fragmento)
	{
		if (fragmento != null)
		{
			if (fragmento instanceof LoadingFragment)
			{
				estado = TEstado.Loading;
				setTitle(R.string.title_app);
			}
			else if (fragmento instanceof MainFragment)
			{
				estado = TEstado.Main;
				setTitle(R.string.title_app);
			}
			else if (fragmento instanceof DesignFragment)
			{
				estado = TEstado.Design;
				setTitle(R.string.title_design_phase);
			}
			else if (fragmento instanceof PaintFragment)
			{
				estado = TEstado.Paint;
				setTitle(R.string.title_paint_phase);
			}
			else if (fragmento instanceof AnimationFragment)
			{
				estado = TEstado.Animation;
				setTitle(R.string.title_animation_phase);
			}
			else if (fragmento instanceof CharacterSelectionFragment)
			{
				estado = TEstado.CharacterSelection;
				setTitle(R.string.title_character_selection_phase);
			}
			else if (fragmento instanceof LevelSelectionFragment)
			{
				estado = TEstado.LevelSelection;
				setTitle(R.string.title_level_selection_phase);
			}
			else if (fragmento instanceof GameFragment)
			{
				estado = TEstado.Game;
				setTitle(R.string.title_game_phase);
			}
		}
	}

	private void actualizarEstado()
	{
		String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
		actualizarEstado(getSupportFragmentManager().findFragmentByTag(tag));
	}
}
