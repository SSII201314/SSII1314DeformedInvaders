package com.game.data;

public class Obstaculo extends Rectangulo
{
	/* Constructora */

	public Obstaculo(int indiceTextura, int idObstaculo)
	{
		tipo = TTipoEntidad.Obstaculo;
		id = idObstaculo;
		textura = indiceTextura;
	}
}
