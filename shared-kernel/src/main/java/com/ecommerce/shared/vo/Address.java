package com.ecommerce.shared.vo;

import com.ecommerce.shared.domain.ValueObject;

import java.util.Objects;

/**
 * Value Object representing a physical address.
 */
public class Address extends ValueObject<Address> {

    private final String country;
    private final String city;
    private final String district;
    private final String postalCode;
    private final String street;
    private final String detail;

    private Address(Builder builder) {
        this.country = builder.country;
        this.city = Objects.requireNonNull(builder.city, "City cannot be null");
        this.district = builder.district;
        this.postalCode = builder.postalCode;
        this.street = Objects.requireNonNull(builder.street, "Street cannot be null");
        this.detail = builder.detail;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getStreet() {
        return street;
    }

    public String getDetail() {
        return detail;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (postalCode != null) sb.append(postalCode).append(" ");
        if (country != null) sb.append(country).append(" ");
        sb.append(city);
        if (district != null) sb.append(district);
        sb.append(street);
        if (detail != null) sb.append(detail);
        return sb.toString().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(country, address.country) &&
                Objects.equals(city, address.city) &&
                Objects.equals(district, address.district) &&
                Objects.equals(postalCode, address.postalCode) &&
                Objects.equals(street, address.street) &&
                Objects.equals(detail, address.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, city, district, postalCode, street, detail);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }

    public static class Builder {
        private String country = "Taiwan";
        private String city;
        private String district;
        private String postalCode;
        private String street;
        private String detail;

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder district(String district) {
            this.district = district;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Address build() {
            return new Address(this);
        }
    }
}
