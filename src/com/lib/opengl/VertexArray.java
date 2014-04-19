package com.lib.opengl;

import com.lib.utils.FloatArray;

public class VertexArray extends FloatArray
{
	public VertexArray()
	{
		super();
	}
	
	public VertexArray(int size)
	{
		super(2 * size);
	}
	
	public VertexArray(FloatArray list)
	{
		super(list);
	}
	
	public void addVertex(float x, float y)
	{
		add(x);
		add(y);
	}
	
	public void setXVertex(int vertex, float x)
	{
		set(2 * vertex, x);
	}
	
	public void setYVertex(int vertex, float y)
	{
		set(2 * vertex + 1, y);
	}
	
	public float getXVertex(int vertex)
	{
		return get(2 * vertex);
	}
	
	public float getYVertex(int vertex)
	{
		return get(2 * vertex + 1);
	}
	
	public float getLastXVertex()
	{
		return get(size - 2);
	}
	
	public float getLastYVertex()
	{
		return get(size - 1);
	}
	
	public void removeVertex(int vertex)
	{
		removeIndex(2 * vertex + 1);
		removeIndex(2 * vertex);
	}
	
	public int getNumVertices()
	{
		return size / 2;
	}
	
	public VertexArray clone()
	{
		return new VertexArray(super.clone());
	}
}
