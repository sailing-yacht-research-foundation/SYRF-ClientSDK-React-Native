/**
 * Represents a heading data.
 */

export interface SYRFHeading {
  headingMagnetic: number;
  headingTrue: number;
  rawData: {
    x: number;
    y: number;
    z: number;
  };
  accuracy: number;
  timestamp: number;
}
