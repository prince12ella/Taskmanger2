import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Classe principale qui étend JFrame pour créer une application GUI
public class ContactManagerGUI extends JFrame {
    private JTextField searchField;
    private JComboBox<String> filterGroupeComboBox, triComboBox;
    private JButton ajouterButton, modifierButton, supprimerButton;
    private DefaultListModel<String> contactsListModel;
    private List<Contact> contactsList;
    private JList<String> contactsJList;

    // Constructeur principal de l'application GUI
    public ContactManagerGUI() {
        setTitle("Gestionnaire de Contacts"); // Titre de la fenêtre
        setSize(600, 400); // Dimensions de la fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Action à la fermeture
        setLocationRelativeTo(null); // Position de la fenêtre centrée

        // Panel principal qui contient tous les composants
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Panel supérieur avec grille pour les filtres et la recherche
        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        // Panel pour les filtres (groupe et tri)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Groupe:")); // Étiquette pour le groupe
        filterGroupeComboBox = new JComboBox<>(new String[]{"Tous", "Famille", "Amis", "Travail"}); // Liste déroulante des groupes
        filterGroupeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filtrerContacts(); // Méthode appelée lorsqu'un groupe est sélectionné
            }
        });
        filterPanel.add(filterGroupeComboBox);

        filterPanel.add(new JLabel("Trier:")); // Étiquette pour le tri
        triComboBox = new JComboBox<>(new String[]{"Trier A-Z", "Trier Z-A"}); // Liste déroulante pour les options de tri
        triComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trierContacts(); // Méthode appelée lorsqu'une option de tri est sélectionnée
            }
        });
        filterPanel.add(triComboBox);

        topPanel.add(filterPanel); // Ajout du panel de filtre au panel supérieur

        // Panel de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher :")); // Étiquette pour la zone de recherche
        searchField = new JTextField(20); // Champ de texte pour la recherche
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rechercherContact(); // Méthode appelée lorsqu'une recherche est lancée
            }
        });
        searchPanel.add(searchField);

        topPanel.add(searchPanel); // Ajout du panel de recherche au panel supérieur

        panel.add(topPanel, BorderLayout.NORTH); // Ajout du panel supérieur au panel principal

        // Liste des contacts avec défilement
        contactsListModel = new DefaultListModel<>();
        contactsJList = new JList<>(contactsListModel);
        contactsJList.addListSelectionListener(e -> afficherDetailsContact()); // Écouteur de sélection pour afficher les détails du contact
        JScrollPane scrollPane = new JScrollPane(contactsJList);

        panel.add(scrollPane, BorderLayout.CENTER); // Ajout de la liste déroulante au centre du panel principal

        // Panel de boutons pour ajouter, modifier et supprimer
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ajouterButton = new JButton("Ajouter"); // Bouton pour ajouter un contact
        ajouterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                afficherDialogAjouterContact(); // Méthode appelée lorsqu'on clique sur le bouton Ajouter
            }
        });
        buttonPanel.add(ajouterButton);

        modifierButton = new JButton("Modifier"); // Bouton pour modifier un contact
        modifierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifierContact(); // Méthode appelée lorsqu'on clique sur le bouton Modifier
            }
        });
        buttonPanel.add(modifierButton);

        supprimerButton = new JButton("Supprimer"); // Bouton pour supprimer un contact
        supprimerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                supprimerContact(); // Méthode appelée lorsqu'on clique sur le bouton Supprimer
            }
        });
        buttonPanel.add(supprimerButton);

        panel.add(buttonPanel, BorderLayout.SOUTH); // Ajout du panel de boutons en bas du panel principal

        add(panel); // Ajout du panel principal à la JFrame
        setVisible(true); // Rendre la fenêtre visible

        contactsList = new ArrayList<>(); // Initialisation de la liste des contacts
        chargerContacts(); // Chargement des contacts depuis un fichier
    }

    // Méthode pour afficher la fenêtre de dialogue d'ajout de contact
    private void afficherDialogAjouterContact() {
        JDialog dialog = new JDialog(this, "Ajouter Contact", true); // Création d'une nouvelle fenêtre de dialogue modale
        dialog.setSize(400, 300); // Dimensions de la fenêtre de dialogue
        dialog.setLayout(new GridLayout(6, 2)); // Layout de grille pour la fenêtre de dialogue

        JTextField prenomField = new JTextField(); // Champ de texte pour le prénom
        JTextField nomField = new JTextField(); // Champ de texte pour le nom
        JTextField telField = new JTextField(); // Champ de texte pour le numéro de téléphone
        JTextField emailField = new JTextField(); // Champ de texte pour l'email
        JComboBox<String> groupeComboBox = new JComboBox<>(new String[]{"Famille", "Amis", "Travail"}); // Liste déroulante pour le groupe

        dialog.add(new JLabel("Prénom:")); // Étiquette pour le prénom
        dialog.add(prenomField); // Champ de texte pour saisir le prénom

        dialog.add(new JLabel("Nom:")); // Étiquette pour le nom
        dialog.add(nomField); // Champ de texte pour saisir le nom

        dialog.add(new JLabel("Numéro de téléphone:")); // Étiquette pour le numéro de téléphone
        dialog.add(telField); // Champ de texte pour saisir le numéro de téléphone

        dialog.add(new JLabel("Email:")); // Étiquette pour l'email
        dialog.add(emailField); // Champ de texte pour saisir l'email

        dialog.add(new JLabel("Groupe:")); // Étiquette pour le groupe
        dialog.add(groupeComboBox); // Liste déroulante pour sélectionner le groupe

        JButton ajouterButton = new JButton("Ajouter"); // Bouton pour ajouter le contact
        ajouterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String prenom = prenomField.getText(); // Récupération du prénom saisi
                String nom = nomField.getText(); // Récupération du nom saisi
                String tel = telField.getText(); // Récupération du numéro de téléphone saisi
                String email = emailField.getText(); // Récupération de l'email saisi
                String groupe = (String) groupeComboBox.getSelectedItem(); // Récupération du groupe sélectionné

                Contact contact = new Contact(prenom, nom, tel, email, groupe); // Création d'un nouvel objet Contact
                contactsList.add(contact); // Ajout du contact à la liste

                refreshContactsList(); // Rafraîchir la liste des contacts dans l'interface
                sauvegarderContacts(); // Sauvegarder les contacts dans un fichier
                dialog.dispose(); // Fermer la fenêtre de dialogue
            }
        });
        dialog.add(ajouterButton); // Ajout du bouton Ajouter à la fenêtre de dialogue

        dialog.setVisible(true); // Rendre la fenêtre de dialogue visible
    }

    // Méthode pour modifier un contact sélectionné
    private void modifierContact() {
        int selectedIndex = contactsJList.getSelectedIndex(); // Récupération de l'indice du contact sélectionné dans la liste
        if (selectedIndex != -1) { // Vérification si un contact est sélectionné
            // Affichage des fenêtres de dialogue pour modifier chaque champ du contact
            String prenom = JOptionPane.showInputDialog(this, "Prénom:", contactsList.get(selectedIndex).getPrenom());
            String nom = JOptionPane.showInputDialog(this, "Nom:", contactsList.get(selectedIndex).getNom());
            String tel = JOptionPane.showInputDialog(this, "Numéro de téléphone:", contactsList.get(selectedIndex).getTelephone());
            String email = JOptionPane.showInputDialog(this, "Email:", contactsList.get(selectedIndex).getEmail());
            String groupe = (String) JOptionPane.showInputDialog(this, "Groupe:", "Modifier Groupe", JOptionPane.QUESTION_MESSAGE, null, new String[]{"Famille", "Amis", "Travail"}, contactsList.get(selectedIndex).getGroupe());

            // Mise à jour du contact sélectionné avec les nouvelles valeurs
            Contact contact = contactsList.get(selectedIndex);
            contact.setPrenom(prenom);
            contact.setNom(nom);
            contact.setTelephone(tel);
            contact.setEmail(email);
            contact.setGroupe(groupe);

            refreshContactsList(); // Rafraîchir la liste des contacts dans l'interface
            sauvegarderContacts(); // Sauvegarder les contacts mis à jour dans un fichier
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un contact à modifier.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Méthode pour supprimer un contact sélectionné
    private void supprimerContact() {
        int selectedIndex = contactsJList.getSelectedIndex(); // Récupération de l'indice du contact sélectionné dans la liste
        if (selectedIndex != -1) { // Vérification si un contact est sélectionné
            contactsList.remove(selectedIndex); // Suppression du contact de la liste

            refreshContactsList(); // Rafraîchir la liste des contacts dans l'interface
            sauvegarderContacts(); // Sauvegarder la liste des contacts mise à jour dans un fichier
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un contact à supprimer.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Méthode pour afficher les détails d'un contact sélectionné
    private void afficherDetailsContact() {
        int selectedIndex = contactsJList.getSelectedIndex(); // Récupération de l'indice du contact sélectionné dans la liste
        if (selectedIndex != -1) { // Vérification si un contact est sélectionné
            Contact contact = contactsList.get(selectedIndex); // Récupération du contact à l'indice sélectionné
            // Affichage des détails du contact dans une boîte de dialogue
            JOptionPane.showMessageDialog(this, "Prénom: " + contact.getPrenom() + "\nNom: " + contact.getNom() + "\nNuméro de téléphone: " + contact.getTelephone() + "\nEmail: " + contact.getEmail() + "\nGroupe: " + contact.getGroupe(), "Détails du contact", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Méthode pour rechercher un contact par nom, prénom, numéro de téléphone ou email
    private void rechercherContact() {
        String searchText = searchField.getText().toLowerCase(); // Récupération du texte de recherche en minuscules
        contactsListModel.clear(); // Effacement de la liste des contacts

        // Filtrage des contacts en fonction du texte de recherche
        List<String> filteredContacts = contactsList.stream()
                .filter(contact -> contact.getNom().toLowerCase().contains(searchText) ||
                        contact.getPrenom().toLowerCase().contains(searchText) ||
                        contact.getTelephone().toLowerCase().contains(searchText) ||
                        contact.getEmail().toLowerCase().contains(searchText))
                .map(Contact::toString) // Conversion des contacts filtrés en chaînes de caractères
                .collect(Collectors.toList()); // Collecte des résultats filtrés dans une liste

        for (String contact : filteredContacts) {
            contactsListModel.addElement(contact); // Ajout des contacts filtrés à la liste d'affichage
        }
    }

    // Méthode pour filtrer les contacts par groupe
    private void filtrerContacts() {
        String selectedGroupe = (String) filterGroupeComboBox.getSelectedItem(); // Récupération du groupe sélectionné
        // Filtrage des contacts en fonction du groupe sélectionné
        List<Contact> filteredContacts = contactsList.stream()
                .filter(contact -> selectedGroupe.equals("Tous") || contact.getGroupe().equals(selectedGroupe))
                .collect(Collectors.toList()); // Collecte des contacts filtrés dans une liste

        refreshContactsList(filteredContacts); // Rafraîchir la liste des contacts avec les contacts filtrés
    }

    // Méthode pour trier les contacts par nom (A-Z ou Z-A)
    private void trierContacts() {
        String selectedTri = (String) triComboBox.getSelectedItem(); // Récupération de l'option de tri sélectionnée
        List<Contact> sortedContacts = new ArrayList<>(contactsList); // Copie de la liste des contacts

        // Tri des contacts en fonction de l'option sélectionnée
        if (selectedTri.equals("Trier A-Z")) {
            sortedContacts.sort((c1, c2) -> c1.getNom().compareToIgnoreCase(c2.getNom())); // Tri A-Z par nom
        } else if (selectedTri.equals("Trier Z-A")) {
            sortedContacts.sort((c1, c2) -> c2.getNom().compareToIgnoreCase(c1.getNom())); // Tri Z-A par nom
        }

        refreshContactsList(sortedContacts); // Rafraîchir la liste des contacts triés
    }

    // Méthode pour rafraîchir la liste des contacts dans l'interface
    private void refreshContactsList() {
        refreshContactsList(contactsList); // Appel de la méthode surchargée avec la liste complète des contacts
    }

    // Méthode surchargée pour rafraîchir la liste des contacts dans l'interface avec une liste spécifique
    private void refreshContactsList(List<Contact> contacts) {
        contactsListModel.clear(); // Effacement de la liste des contacts affichée
        for (Contact contact : contacts) {
            contactsListModel.addElement(contact.toString()); // Ajout des contacts à la liste d'affichage
        }
    }

    // Méthode pour charger les contacts à partir d'un fichier JSON
    private void chargerContacts() {
        try {
            if (Files.exists(Paths.get("contacts.json"))) { // Vérification de l'existence du fichier de contacts
                List<String> lines = Files.readAllLines(Paths.get("contacts.json")); // Lecture des lignes du fichier
                for (String line : lines) {
                    String[] parts = line.split(","); // Séparation des parties de chaque ligne par ","
                    if (parts.length == 5) { // Vérification du format des données
                        // Création et ajout d'un nouvel objet Contact à partir des données lues
                        contactsList.add(new Contact(parts[0], parts[1], parts[2], parts[3], parts[4]));
                    }
                }
                refreshContactsList(); // Rafraîchir la liste des contacts affichés dans l'interface
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des contacts : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Méthode pour sauvegarder les contacts dans un fichier JSON
    private void sauvegarderContacts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("contacts.json"))) {
            for (Contact contact : contactsList) {
                // Écriture des données de chaque contact dans le fichier, séparées par ","
                writer.write(contact.getPrenom() + "," + contact.getNom() + "," + contact.getTelephone() + "," + contact.getEmail() + "," + contact.getGroupe());
                writer.newLine(); // Nouvelle ligne pour chaque contact
            }
            JOptionPane.showMessageDialog(this, "Contacts sauvegardés avec succès !"); // Message de confirmation de sauvegarde
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde des contacts : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Méthode principale pour lancer l'application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ContactManagerGUI(); // Création d'une instance de ContactManagerGUI
            }
        });
    }
}
