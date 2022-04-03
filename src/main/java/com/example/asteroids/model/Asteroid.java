package com.example.asteroids.model;


public class Asteroid {
	
	private String nombre;
	private double diametro;
	private double velocidad;
	private String fecha;
	private String planeta;
	
	public Asteroid() {}
	
	public Asteroid(String nombre, double diametro, double velocidad, String fecha, String planeta) {
		this.nombre = nombre;
		this.diametro = diametro;
		this.velocidad = velocidad;
		this.fecha = fecha;
		this.planeta = planeta;
	}
	
	public String getName() {
		return this.nombre;
	}
	
	public double getDiameter() {
		return this.diametro;
	}
	
	public double getVelocity() {
		return this.velocidad;
	}
	
	public String getDate() {
		return this.fecha;
	}
	
	public String getPlanet() {
		return this.planeta;
	}
}
