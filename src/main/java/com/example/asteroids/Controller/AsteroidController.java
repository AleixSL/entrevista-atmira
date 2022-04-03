package com.example.asteroids.Controller;

import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;

import org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.asteroids.model.Asteroid;

@RestController
public class AsteroidController {

//	private ArrayList<Asteroid> allAsteroids = new ArrayList<Asteroid>(); //ArrayList de tots els  asteroides
	private ArrayList<Asteroid> topThreeAsteroids = new ArrayList<Asteroid>(3); //ArrayList dels 3 millors asteroides
	
	@GetMapping("/asteroids{days}")
	@ResponseBody
	public ResponseEntity getAsteroids(@RequestParam(value = "days", required = false) Integer days) throws JSONException, MalformedURLException, IOException {
		this.topThreeAsteroids.removeAll(this.topThreeAsteroids);
        if(days >=1 && days <= 7) {

        	// Aconseguim la data actual i la final
        	LocalDate startDate =  LocalDate.now();
    		LocalDate endDate =  LocalDate.now().plusDays(days); 
            
            // Creem la URL amb les dates
            String extUrl = "https://api.nasa.gov/neo/rest/v1/feed?start_date=" + startDate + "&end_date=" + endDate + "&api_key=zdUP8ElJv1cehFM0rsZVSQN7uBVxlDnu4diHlLSb";         
						
			// Descarreguem el JSON de la URL
			JSONObject json = new JSONObject(IOUtils.toString(new URL(extUrl), Charset.forName("UTF-8")));
			JSONObject asteroids = json.getJSONObject("near_earth_objects");
		    	    
		    // Busquem el top 3 d'asteroides amb una funció externa
		    getTopThreeAsteroids(asteroids, days);

			
		    return new ResponseEntity<ArrayList<Asteroid> >(this.topThreeAsteroids, HttpStatus.OK);
		}
        
    	return new ResponseEntity<String> ("Days parameter should be from 1 to 7.", HttpStatus.BAD_REQUEST);
	}

	private void getTopThreeAsteroids(JSONObject json, Integer days) {
		
		for (int i = 0; i < days + 1; i++) {
			// Aconseguim la data actual + i per a trobar-la al JSON. Així podem aconseguir l'array de cada dia.
			LocalDate jsonDate =  LocalDate.now().plusDays(i);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			JSONArray dateArray = json.getJSONArray(jsonDate.format(formatter));
			
			int count = dateArray.length()-1;
			for (int j = 0; j < count; j++) {			
				JSONObject jsonAsteroid = new JSONObject();
				jsonAsteroid = dateArray.getJSONObject(j);
				Boolean isHazardous = jsonAsteroid.getBoolean("is_potentially_hazardous_asteroid");
				
				if (isHazardous) {
					double d = getDiameter(jsonAsteroid);
//					addAsteroid(jsonAsteroid, d);
					if (this.topThreeAsteroids.size() < 3) {
						addAsteroid(jsonAsteroid, d, 1000);
					}
					else {
						sortArrayList();
						// Recorrem l'array list per a comparar diàmetres, si és més gran borrem l'altre i afegim el nou
						for (int x = 0; x < this.topThreeAsteroids.size()-1; x++) {		
							Asteroid asAct = new Asteroid();
							asAct = this.topThreeAsteroids.get(x);
							if (asAct.getDiameter() < d) {
								if(x < this.topThreeAsteroids.size()-1) {
									Asteroid asNext = new Asteroid();
									asNext = this.topThreeAsteroids.get(x+1);
									if(asNext.getDiameter() > d) {
										addAsteroid(jsonAsteroid, d, x);
									}
								}
								else addAsteroid(jsonAsteroid, d, x);
							}
						}
					}
				}
			}
		}
//		sortArrayList();
//		int n = Math.min(3, allAsteroids.size());
//		for(int i = 0; i < n; i++) {
//			this.topThreeAsteroids.add(allAsteroids.get(i)); 
//		}
	}

	private void sortArrayList() {
//		Collections.sort(this.allAsteroids, new Comparator<Asteroid>() {
//			@Override
//			public int compare(Asteroid a1, Asteroid a2) {
//				return Double.compare(a1.getDiameter(), a2.getDiameter());
//			}
//		});
		int arrSize = this.topThreeAsteroids.size();
	    for (int i = 0; i < arrSize-1; i++) {
	    	for (int j = 0; j < arrSize-1-i; j++) {
		        if (this.topThreeAsteroids.get(j).getDiameter() > this.topThreeAsteroids.get(j+1).getDiameter()) {
		        	swap(this.topThreeAsteroids.get(j), this.topThreeAsteroids.get(j+1));
		        }
	    	}
	    }		    
	}
	
	private void swap(Asteroid a1, Asteroid a2) {
	    Asteroid temp = a1;
	    a1 = a2;
	    a2 = temp;
	}

	private double getDiameter(JSONObject jsonAsteroid) {
		
		JSONObject jsonDiameters = jsonAsteroid.getJSONObject("estimated_diameter");
		JSONObject jsonKiloDiameters = jsonDiameters.getJSONObject("kilometers");
		
		double maxD =  jsonKiloDiameters.getDouble("estimated_diameter_max");
		double minD =  jsonKiloDiameters.getDouble("estimated_diameter_min");
		
		double d = (maxD + minD) / 2;
		
		return d;
	}

	private void addAsteroid(JSONObject jsonAsteroid, double d, int pos) {
			
		// Agafem els objectes del JSON que ens interessen
		JSONArray jsonCloseApproach = jsonAsteroid.getJSONArray("close_approach_data");
		JSONObject jsonCloseApproach0 = jsonCloseApproach.getJSONObject(0);
		JSONObject jsonVelocity = jsonCloseApproach0.getJSONObject("relative_velocity");
		
		// Guardem els valors que volem
		String nombre = jsonAsteroid.getString("name");
		double diametro = d;
		double velocidad = jsonVelocity.getDouble("kilometers_per_hour");
		String fecha = jsonCloseApproach0.getString("close_approach_date");
		String planeta = jsonCloseApproach0.getString("orbiting_body");
		
		// Creem el nou objecte Asteroide i l'afegim a l'array list dels top 3 asteroides
		Asteroid a = new Asteroid(nombre, diametro, velocidad, fecha, planeta);
		if(pos == 1000) {
			this.topThreeAsteroids.add(a);
		}
		else {
			this.topThreeAsteroids.remove(pos);
			this.topThreeAsteroids.add(a);
		}
	}
	
}
