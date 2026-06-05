package org.example.clinic.client.ui;

import java.util.function.Supplier;


public record NavigationItem(String title, Supplier<View> viewSupplier) {
}
