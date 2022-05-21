package com.shelter.animalback.contract.api.animal;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.List;

import com.shelter.animalback.controller.AnimalController;
import com.shelter.animalback.controller.dto.AnimalDto;
import com.shelter.animalback.domain.Animal;
import com.shelter.animalback.model.AnimalDao;
import com.shelter.animalback.repository.AnimalRepository;
import com.shelter.animalback.service.interfaces.AnimalService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@PactBroker(url = "${PACT_BROKER_BASE_URL}", authentication = @PactBrokerAuth(token = "${PACT_BROKER_TOKEN}"))
@Provider("AnimalShelterBack")
@ExtendWith(MockitoExtension.class)
public class AnimalTest {

    @Mock
    private AnimalService animalService;
    @InjectMocks
    private AnimalController animalController;
    @Autowired
    private AnimalRepository repository;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    public void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    public void changeContext(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(animalController);
        context.setTarget(testTarget);
        System.setProperty("pact.verifier.publishResults", "true");
    }

    @State("has animals")
    public void listAnimals() {
        Animal animal = new Animal();
        animal.setName("Bigotes");
        animal.setBreed("Siames");
        animal.setGender("Male");
        animal.setVaccinated(false);
        List<Animal> animals = new ArrayList<Animal>();
        animals.add(animal);
        Mockito.when(animalService.getAll()).thenReturn(animals);
    }

    @State("has animal with name = Bigotes")
    public void listAnimalByName() {
        Animal animal = new Animal();
        animal.setName("Bigotes");
        animal.setBreed("Siames");
        animal.setGender("Male");
        animal.setVaccinated(true);
        Mockito.when(animalService.get("bigotes")).thenReturn(animal);
    }

    @State("adds animal with name = addedAnimal")
    public void addAnimal() {
        Animal animal = new Animal();
        animal.setName("addedAnimal");
        animal.setBreed("addedAnimalBreed");
        animal.setGender("Female");
        animal.setVaccinated(false);
        Mockito.lenient().when(animalService.save(any(Animal.class)))
                .thenReturn(animal);
    }

    @State("updates the animal with name = Manchas")
    public void updateAnimal() {
        Animal animal = new Animal();
        animal.setId(1);
        animal.setName("manchas");
        animal.setBreed("Bengali");
        animal.setGender("Female");
        animal.setVaccinated(true);
        String[] vaccines = { "lupus", "rabia" };
        animal.setVaccines(vaccines);
        Mockito.when(animalService.replace(eq("manchas"), any(Animal.class)))
                .thenReturn(animal);
    }

    @State("deletes animal with name = Bigotes")
    public void deleteAnimalByName() {
        Mockito.doNothing().when(animalService).delete("bigotes");
    }

    private Animal map(AnimalDao dao) {
        var vaccinesDao = dao.getVaccines();

        var vaccines = vaccinesDao == null ? new String[0]
                : vaccinesDao.stream().map(vaccineDao -> vaccineDao.getName()).toArray(size -> new String[size]);

        return new Animal(
                dao.getId(),
                dao.getName(),
                dao.getBreed(),
                dao.getGender(),
                dao.isVaccinated(),
                vaccines);
    }

    private AnimalDao map(Animal animal) {
        return new AnimalDao(
                animal.getName(),
                animal.getBreed(),
                animal.getGender(),
                animal.isVaccinated());
    }
}
