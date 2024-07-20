import CoreMedia

extension CMTime {
    var isValidCMTime: Bool {
        isValid == true &&
        isNegativeInfinity == false &&
        isPositiveInfinity == false &&
        isIndefinite == false &&
        isNumeric == true
    }
}
