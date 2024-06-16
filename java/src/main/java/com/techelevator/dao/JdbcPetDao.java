package com.techelevator.dao;

import com.techelevator.model.Pet;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcPetDao implements PetDAO {
    private JdbcTemplate template;

    public JdbcPetDao(DataSource ds) {
        template = new JdbcTemplate(ds);
    }
    private Pet mapRowToPet(SqlRowSet sqlRowSet) {
        Pet pet = new Pet();

        pet.setPetId(sqlRowSet.getInt("pet_id"));
        pet.setName(sqlRowSet.getString("name"));
        pet.setSpecies(sqlRowSet.getString("species"));
        pet.setBreed(sqlRowSet.getString("breed"));
        pet.setWeight(sqlRowSet.getString("weight"));
        pet.setGender(sqlRowSet.getString("gender"));
        pet.setAge(sqlRowSet.getString("age"));
        pet.setSpayedNeutered(sqlRowSet.getBoolean("spayed_neutered"));

        return pet;
    }


    @Override
    public List<Pet> getAllPets() {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT * FROM pets";

        try {
            SqlRowSet results = template.queryForRowSet(sql);
            while (results.next()) {
                Pet pet = mapRowToPet(results);

                List<String> petDescriptions = getPetDescriptions(pet.getPetId());
                pet.setPetDescriptions(petDescriptions);

                List<String> petImageUrls = getPetImageUrls(pet.getPetId());
                pet.setPetImageUrls(petImageUrls);

                pets.add(pet);
            }
        } catch (CannotGetJdbcConnectionException e) {
            System.out.println("Problem connecting");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Data problems");
        }
        return pets;
    }

    @Override
    public Pet getPet(int petId) {
        Pet pet = new Pet();
        String sql = "SELECT * FROM pets WHERE pet_id = ?;";

        try {
            SqlRowSet results = template.queryForRowSet(sql, petId);
            if (results.next()) {
                pet = mapRowToPet(results);

                List<String> petDescriptions = getPetDescriptions(pet.getPetId());
                pet.setPetDescriptions(petDescriptions);

                List<String> petImageUrls = getPetImageUrls(pet.getPetId());
                pet.setPetImageUrls(petImageUrls);
            }
        } catch (CannotGetJdbcConnectionException e) {
            System.out.println("Problem connecting to database");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Problem with Data Integrity");
        }
        return pet;
    }


    @Override
    public List<String> getPetDescriptions(int petId) {
        List<String> petDescriptions = new ArrayList<>();
        String sql = "SELECT description FROM descriptions " +
                "JOIN pet_description ON pet_description.description_id = descriptions.description_id " +
                "WHERE pet_id = ?;";

        try {
            SqlRowSet results = template.queryForRowSet(sql, petId);
            while (results.next()) {
                petDescriptions.add(results.getString("description"));
            }
        } catch (CannotGetJdbcConnectionException e) {
            System.out.println("Problem connecting to database");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Problem with Data Integrity");
        }
        return petDescriptions;
    };

    @Override
    public List<String> getDescriptions() {
        List<String> descriptions = new ArrayList<>();
        String sql = "SELECT description FROM descriptions ";

        try {
            SqlRowSet results = template.queryForRowSet(sql);
            while (results.next()) {
                descriptions.add(results.getString("description"));
            }
        } catch (CannotGetJdbcConnectionException e) {
            System.out.println("Problem connecting to database");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Problem with Data Integrity");
        }
        return descriptions;
    };


    public List<String> getPetImageUrls(int petId) {
        List<String> petImageUrls = new ArrayList<>();
        // When we change it to multiple images per pet we'll need a
        // JOIN with the associative table that connects pets to images
        String sql = "SELECT image_url FROM images WHERE pet_id = ?;";

        try {
            SqlRowSet results = template.queryForRowSet(sql, petId);
            if (results.next()) {
                petImageUrls.add(results.getString("image_url"));
            }
        } catch (CannotGetJdbcConnectionException e) {
            System.out.println("Problem connecting to database");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Problem with Data Integrity");
        }
        return petImageUrls;
    }


    @Override
    public Pet addPet(Pet petToAdd) {
        String sql = "INSERT INTO pets " +
                "(name, species, breed, weight, gender, age, spayed_neutered)" +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?) RETURNING pet_id";
        int newPetId = -1;
        try {
            newPetId = template.queryForObject(sql, Integer.class,
                    petToAdd.getName(),
                    petToAdd.getSpecies(),
                    petToAdd.getBreed(),
                    petToAdd.getWeight(),
                    petToAdd.getGender(),
                    petToAdd.getAge(),
                    petToAdd.getSpayedNeutered()
            );
        } catch (CannotGetJdbcConnectionException e) {
            System.out.println("Problem connecting to database");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Problem with Data Integrity");
            System.out.println(e.getMessage());
        }
        return getPet(newPetId);
    };

    @Override
    public boolean addPetDescriptions(int petId, String description) {
        int rowAffected = 0;
        String sql = "INSERT INTO pet_description (pet_id, description_id) " +
                "SELECT p.pet_id, d.description_id " +
                "FROM pets AS p " +
                "JOIN descriptions AS d ON d.description = ? " +
                "WHERE p.pet_id = ?;";
        try {
            rowAffected = template.update(sql, description, petId);
        } catch (CannotGetJdbcConnectionException e) {
            System.out.println("Problem connecting to database");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Problem with Data Integrity");
            System.out.println(e.getMessage());
        }
        return rowAffected > 0;
    }
}

