package org.example;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static Session session;

    public static void main(String[] args) {
        session = HibernateUtil.getSessionFactory().openSession();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Введите команду: ");
            String command = scanner.nextLine();
            if (command.startsWith("/showProductsByPerson ")) {
                String name = command.split(" ")[1];
                showProductsByPerson(name);
            } else if (command.startsWith("/findPersonsByProductTitle ")) {
                String title = command.split(" ")[1];
                findPersonsByProductTitle(title);
            } else if (command.startsWith("/removePerson ")) {
                String name = command.split(" ")[1];
                removePerson(name);
            } else if (command.startsWith("/removeProduct ")) {
                String title = command.split(" ")[1];
                removeProduct(title);
            } else if (command.startsWith("/buy ")) {
                String[] parts = command.split(" ");
                String customerName = parts[1];
                String productTitle = parts[2];
                buy(customerName, productTitle);
            } else {
                System.out.println("Неизвестная команда.");
            }
        }
    }

    public static void showProductsByPerson(String name) {
        List<Purchase> purchases = session.createQuery(
                        "FROM Purchase p WHERE p.customer.name = :name", Purchase.class)
                .setParameter("name", name)
                .getResultList();
        System.out.println("Список товаров покупателя " + name + ":");
        for (Purchase purchase : purchases) {
            System.out.println(purchase.getProduct().getTitle() + " - " + purchase.getPurchasePrice());
        }
    }

    public static void findPersonsByProductTitle(String title) {
        List<Customer> customers = session.createQuery(
                        "SELECT DISTINCT p.customer FROM Purchase p WHERE p.product.title = :title", Customer.class)
                .setParameter("title", title)
                .getResultList();
        System.out.println("Список покупателей, купивших товар " + title + ":");
        for (Customer customer : customers) {
            System.out.println(customer.getName());
        }
    }

    public static void removePerson(String name) {
        Transaction transaction = session.beginTransaction();
        Customer customer = session.createQuery(
                        "FROM Customer c WHERE c.name = :name", Customer.class)
                .setParameter("name", name)
                .uniqueResult();

        if (customer != null) {
            session.remove(customer);
        }

        transaction.commit();
    }

    public static void removeProduct(String title) {
        Transaction transaction = session.beginTransaction();
        Product product = session.createQuery(
                        "FROM Product p WHERE p.title = :title", Product.class)
                .setParameter("title", title)
                .uniqueResult();

        if (product != null) {
            session.remove(product);
        }

        transaction.commit();
    }

    public static void buy(String customerName, String productTitle) {
        Transaction transaction = session.beginTransaction();

        Customer customer = session.createQuery(
                        "FROM Customer c WHERE c.name = :name", Customer.class)
                .setParameter("name", customerName)
                .uniqueResult();

        Product product = session.createQuery(
                        "FROM Product p WHERE p.title = :title", Product.class)
                .setParameter("title", productTitle)
                .uniqueResult();

        if (customer != null && product != null) {
            Purchase purchase = new Purchase();
            purchase.setCustomer(customer);
            purchase.setProduct(product);
            purchase.setPurchasePrice(product.getPrice());
            session.persist(purchase);
            System.out.println("Покупатель " + customerName + " купил(а) товар " + productTitle);
        }

        transaction.commit();
    }
}
